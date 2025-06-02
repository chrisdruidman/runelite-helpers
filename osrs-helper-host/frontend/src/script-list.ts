import { LitElement, html, css } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { ListLuaScripts, StartScript, StopScript } from '../wailsjs/go/main/App';

@customElement('script-list')
export class ScriptList extends LitElement {
    static styles = css`
        .script-list {
            margin: 2rem auto;
            max-width: 600px;
            background: #232b3a;
            border-radius: 8px;
            padding: 1rem 2rem;
            color: #fff;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        }
        .script-list h2 {
            margin-top: 0;
            font-size: 1.3rem;
            color: #aee6ff;
        }
        .script-list ul {
            list-style: none;
            padding: 0;
        }
        .script-list li {
            margin: 0.5rem 0;
            padding: 0.5rem 0.5rem;
            border-bottom: 1px solid #334;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .script-list .modtime {
            font-size: 0.9em;
            color: #b0b0b0;
        }
        .script-actions {
            display: flex;
            gap: 0.5rem;
        }
        button {
            background: #2d8fff;
            color: #fff;
            border: none;
            border-radius: 4px;
            padding: 0.3rem 0.8rem;
            font-size: 1em;
            cursor: pointer;
            transition: background 0.2s;
        }
        button:disabled {
            background: #444a;
            color: #aaa;
            cursor: not-allowed;
        }
        .status {
            font-size: 0.9em;
            color: #aee6ff;
            margin-left: 0.5rem;
        }
    `;

    @state()
    scripts: Array<{ name: string; modTime: string }> = [];
    @state()
    running: Record<string, boolean> = {};
    @state()
    statusMsg: Record<string, string> = {};

    connectedCallback() {
        super.connectedCallback();
        this.loadScripts();
    }

    async loadScripts() {
        const scripts = await ListLuaScripts();
        // Format modTime for display
        this.scripts = scripts.map((s: any) => ({
            name: s.name,
            modTime: new Date(s.modTime).toLocaleString(),
        }));
    }

    async startScript(name: string) {
        this.statusMsg = { ...this.statusMsg, [name]: 'Starting...' };
        this.running = { ...this.running, [name]: true };
        const result = await StartScript(name);
        this.statusMsg = { ...this.statusMsg, [name]: result };
    }

    async stopScript(name: string) {
        this.statusMsg = { ...this.statusMsg, [name]: 'Stopping...' };
        const result = await StopScript(name);
        this.statusMsg = { ...this.statusMsg, [name]: result };
        this.running = { ...this.running, [name]: false };
    }

    render() {
        return html`
            <div class="script-list">
                <h2>Lua Scripts</h2>
                <ul>
                    ${this.scripts.length === 0
                        ? html`<li>No scripts found.</li>`
                        : this.scripts.map(
                              (script) => html`
                                  <li>
                                      <span>${script.name}</span>
                                      <span class="modtime">${script.modTime}</span>
                                      <span class="script-actions">
                                          <button
                                              @click=${() => this.startScript(script.name)}
                                              ?disabled=${this.running[script.name]}
                                          >
                                              Start
                                          </button>
                                          <button
                                              @click=${() => this.stopScript(script.name)}
                                              ?disabled=${!this.running[script.name]}
                                          >
                                              Stop
                                          </button>
                                      </span>
                                      ${this.statusMsg[script.name]
                                          ? html`<span class="status">${this.statusMsg[script.name]}</span>`
                                          : ''}
                                  </li>
                              `
                          )}
                </ul>
            </div>
        `;
    }
}
