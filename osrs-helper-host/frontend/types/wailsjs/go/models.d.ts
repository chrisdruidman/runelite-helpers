export declare namespace main {
    class ScriptInfo {
        name: string;
        modTime: any;
        static createFrom(source?: any): ScriptInfo;
        constructor(source?: any);
        convertValues(a: any, classs: any, asMap?: boolean): any;
    }
}
