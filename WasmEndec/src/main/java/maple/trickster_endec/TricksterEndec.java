package maple.trickster_endec;


import maple.trickster_endec.fragment.SpellPart;
import org.teavm.jso.JSExport;
import io.wispforest.endec.format.gson.GsonSerializer;

public class TricksterEndec {
    public static void main(String[] args) {
        System.out.println(convertBase64SpellToJson("YwqT9+ZnAAEAZjD29AoAAAA="));
    }

    @JSExport
    public static String convertBase64SpellToJson(String base64String) {

        return SpellPart.fromBase64(base64String).toString();
    }

}