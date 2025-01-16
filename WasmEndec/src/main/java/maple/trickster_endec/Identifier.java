package maple.trickster_endec;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;

public class Identifier {
    public static Endec<Identifier> ENDEC = Endec.STRING.xmap(Identifier::of, Identifier::toString);

    private final String namespace;
    private final String path;

    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }


    public static Identifier of(String id) {
        return splitOn(id, ':');
    }

    public static Identifier splitOn(String id, char delimiter) {
        int i = id.indexOf(delimiter);
        if (i >= 0) {
            String string = id.substring(i + 1);
            if (i != 0) {
                String string2 = id.substring(0, i);
                return new Identifier(string2, string);
            }
        }
        throw new RuntimeException();
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    public String toString() {
        return this.namespace + ":" + this.path;
    }

}
