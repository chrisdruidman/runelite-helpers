import { LitElement } from 'lit';
export declare class ScriptLog extends LitElement {
    static styles: import("lit").CSSResult;
    log: string[];
    intervalId: any;
    connectedCallback(): void;
    disconnectedCallback(): void;
    loadLog(): Promise<void>;
    render(): import("lit-html").TemplateResult<1>;
}
