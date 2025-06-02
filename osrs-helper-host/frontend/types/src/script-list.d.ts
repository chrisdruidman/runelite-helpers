import { LitElement } from 'lit';
export declare class ScriptList extends LitElement {
    static styles: import("lit").CSSResult;
    scripts: Array<{
        name: string;
        modTime: string;
    }>;
    running: Record<string, boolean>;
    statusMsg: Record<string, string>;
    connectedCallback(): void;
    loadScripts(): Promise<void>;
    startScript(name: string): Promise<void>;
    stopScript(name: string): Promise<void>;
    render(): import("lit-html").TemplateResult<1>;
}
