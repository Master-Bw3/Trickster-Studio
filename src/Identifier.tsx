export class Identifier {
    namespace: string;
    path: string;

    constructor(namespace: string, path: string) {
        this.namespace = namespace;
        this.path = path;
    }

    public toString() {
        return this.namespace + ':' + this.path;
    }
}
