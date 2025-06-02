import { LitElement, html, css } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { GetLog } from '../wailsjs/go/main/App';

@customElement('script-log')
export class ScriptLog extends LitElement {
    static styles = css`
        .log-area {
            background: #181f2a;
            color: #e0e0e0;
            font-family: monospace;
            font-size: 0.95em;
            border-radius: 6px;
            padding: 1rem;
            margin: 2rem auto;
            max-width: 700px;
            min-height: 120px;
            max-height: 300px;
            overflow-y: auto;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
        }
        .log-line {
            white-space: pre-wrap;
            margin: 0;
        }
    `;

    @state()
    log: string[] = [];

    intervalId: any;

    connectedCallback() {
        super.connectedCallback();
        this.loadLog();
        this.intervalId = setInterval(() => this.loadLog(), 4000); // Poll every 4 seconds
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        clearInterval(this.intervalId);
    }

    async loadLog() {
        this.log = await GetLog();
    }

    render() {
        return html`
            <div class="log-area">
                <button @click=${() => this.loadLog()} style="float:right;margin-bottom:0.5rem;">Refresh</button>
                ${this.log.length === 0
                    ? html`<div class="log-line">No script output yet.</div>`
                    : this.log.map((line) => html`<div class="log-line">${line}</div>`)}
            </div>
        `;
    }
}
