package maple.trickster_endec;


import maple.trickster_endec.endecs.JSONSerializer;
import maple.trickster_endec.fragment.Fragment;
import maple.trickster_endec.fragment.Pattern;
import maple.trickster_endec.fragment.PatternGlyph;
import maple.trickster_endec.fragment.SpellPart;
import org.teavm.jso.JSExport;
import org.teavm.jso.JSExportClasses;

@JSExportClasses({Fragment.class, SpellPart.class, Pattern.class, PatternGlyph.class, Pattern.PatternEntry.class})
public class TricksterEndec {
    public static void main(String[] args) {
    }

    @JSExport
    public static SpellPart decodeBase64Spell(String base64String) {

        return SpellPart.fromBase64(base64String);
    }

}