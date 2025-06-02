package main

import (
	"context"
	"fmt"
	"github.com/fsnotify/fsnotify"
	"github.com/yuin/gopher-lua"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"time"
)

// App struct
type App struct {
	ctx      context.Context
	logBuffer []string
	logMutex  sync.Mutex

	runningScripts map[string]context.CancelFunc // script name -> cancel func
	runningMutex   sync.Mutex
}

// NewApp creates a new App application struct
func NewApp() *App {
	if defaultApp == nil {
		defaultApp = &App{
			runningScripts: make(map[string]context.CancelFunc),
		}
	}
	return defaultApp
}

// startup is called when the app starts. The context is saved
// so we can call the runtime methods
func (a *App) startup(ctx context.Context) {
	a.ctx = ctx
}

// Greet returns a greeting for the given name
func (a *App) Greet(name string) string {
	return fmt.Sprintf("Hello %s, It's show time!", name)
}

// RunLuaScript executes a Lua script string and returns the result or error
func (a *App) RunLuaScript(script string) string {
	L := lua.NewState()
	defer L.Close()
	if err := L.DoString(script); err != nil {
		return fmt.Sprintf("Lua error: %s", err)
	}
	return "Lua script executed successfully."
}

// AppendLog appends a message to the log buffer (thread-safe)
func (a *App) AppendLog(msg string) {
	a.logMutex.Lock()
	defer a.logMutex.Unlock()
	a.logBuffer = append(a.logBuffer, msg)
	if len(a.logBuffer) > 200 {
		a.logBuffer = a.logBuffer[len(a.logBuffer)-200:]
	}
}

// ClearLog clears the log buffer (thread-safe)
func (a *App) ClearLog() {
	a.logMutex.Lock()
	defer a.logMutex.Unlock()
	a.logBuffer = nil
}

// GetLog returns the current log buffer
func (a *App) GetLog() []string {
	a.logMutex.Lock()
	defer a.logMutex.Unlock()
	return append([]string{}, a.logBuffer...)
}

// WatchScriptsDir watches the scripts directory for changes (no longer auto-runs scripts)
func (a *App) WatchScriptsDir() {
	dir := "scripts"
	if _, err := os.Stat(dir); os.IsNotExist(err) {
		fmt.Println("scripts directory does not exist, skipping watch")
		return
	}
	watcher, err := fsnotify.NewWatcher()
	if err != nil {
		fmt.Println("Failed to create watcher:", err)
		return
	}
	defer watcher.Close()
	done := make(chan bool)
	go func() {
		for {
			select {
			case event, ok := <-watcher.Events:
				if !ok {
					return
				}
				if filepath.Ext(event.Name) == ".lua" && (event.Op&fsnotify.Write == fsnotify.Write || event.Op&fsnotify.Create == fsnotify.Create) {
					msg := fmt.Sprintf("Script file changed: %s", event.Name)
					fmt.Println(msg)
					a.AppendLog(msg)
					// No auto-run
				}
			case err, ok := <-watcher.Errors:
				if !ok {
					return
				}
				msg := fmt.Sprintf("Watcher error: %s", err)
				fmt.Println(msg)
				a.AppendLog(msg)
			}
		}
	}()
	watcher.Add(dir)
	// No initial load of scripts
	<-done // Block forever (or until app shutdown)
}

// setLuaPrintOverride overrides the Lua print function to append output to the app's log
func setLuaPrintOverride(L *lua.LState, app *App) {
	L.SetGlobal("print", L.NewFunction(func(L *lua.LState) int {
		n := L.GetTop()
		args := make([]string, n)
		for i := 1; i <= n; i++ {
			args[i-1] = L.ToStringMeta(L.Get(i)).String()
		}
		msg := strings.Join(args, "\t")
		if app != nil {
			app.AppendLog("[lua] " + msg)
		} else if defaultApp != nil {
			defaultApp.AppendLog("[lua] " + msg)
		}
		return 0
	}))
}

// loadAndRunLuaScript loads and executes a Lua script file
func loadAndRunLuaScript(path string) {
	L := lua.NewState()
	defer L.Close()
	setLuaPrintOverride(L, defaultApp)
	var output string
	if err := L.DoFile(path); err != nil {
		output = fmt.Sprintf("Lua error in %s: %s", path, err)
	} else {
		output = fmt.Sprintf("Executed %s successfully.", path)
	}
	defaultApp.AppendLog(output)
}

// loadAndRunLuaScriptWithApp loads and executes a Lua script file, appending output to the app's log
func loadAndRunLuaScriptWithApp(app *App, path string) {
	L := lua.NewState()
	defer L.Close()
	setLuaPrintOverride(L, app)
	if err := L.DoFile(path); err != nil {
		msg := fmt.Sprintf("Lua error in %s: %s", path, err)
		fmt.Println(msg)
		app.AppendLog(msg)
	} else {
		msg := fmt.Sprintf("Executed %s successfully.", path)
		fmt.Println(msg)
		app.AppendLog(msg)
	}
}

// ScriptInfo holds metadata about a Lua script file
type ScriptInfo struct {
	Name    string    `json:"name"`
	ModTime time.Time `json:"modTime"`
}

// ListLuaScripts returns a list of all Lua scripts in the scripts directory with their metadata
func (a *App) ListLuaScripts() []ScriptInfo {
	dir := "scripts"
	files, err := filepath.Glob(filepath.Join(dir, "*.lua"))
	if err != nil {
		return nil
	}
	var scripts []ScriptInfo
	for _, file := range files {
		info, err := os.Stat(file)
		if err != nil {
			continue
		}
		scripts = append(scripts, ScriptInfo{
			Name:    filepath.Base(file),
			ModTime: info.ModTime(),
		})
	}
	return scripts
}

// StartScript starts a Lua script by filename (non-blocking, tracked)
func (a *App) StartScript(name string) string {
	scriptPath := filepath.Join("scripts", name)
	if _, err := os.Stat(scriptPath); err != nil {
		return fmt.Sprintf("Script not found: %s", name)
	}
	a.ClearLog() // Clear log before running
	a.runningMutex.Lock()
	if _, exists := a.runningScripts[name]; exists {
		a.runningMutex.Unlock()
		return fmt.Sprintf("Script already running: %s", name)
	}
	ctx, cancel := context.WithCancel(context.Background())
	a.runningScripts[name] = cancel
	a.runningMutex.Unlock()

	go func() {
		L := lua.NewState()
		defer L.Close()
		setLuaPrintOverride(L, a)
		ch := make(chan error, 1)
		go func() {
			ch <- L.DoFile(scriptPath)
		}()
		select {
		case <-ctx.Done():
			a.AppendLog(fmt.Sprintf("Stopped script: %s", name))
		case err := <-ch:
			if err != nil {
				a.AppendLog(fmt.Sprintf("Lua error in %s: %s", name, err))
			} else {
				a.AppendLog(fmt.Sprintf("Executed %s successfully.", name))
			}
		}
		a.runningMutex.Lock()
		delete(a.runningScripts, name)
		a.runningMutex.Unlock()
	}()
	return fmt.Sprintf("Started script: %s", name)
}

// StopScript stops a running Lua script by filename
func (a *App) StopScript(name string) string {
	a.runningMutex.Lock()
	cancel, exists := a.runningScripts[name]
	a.runningMutex.Unlock()
	if !exists {
		return fmt.Sprintf("Script not running: %s", name)
	}
	cancel()
	return fmt.Sprintf("Stop requested for script: %s", name)
}

// defaultApp is the global app instance for logging from static functions
var defaultApp *App
