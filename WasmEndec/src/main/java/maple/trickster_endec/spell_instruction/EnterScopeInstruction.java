package maple.trickster_endec.spell_instruction;

public class EnterScopeInstruction implements SpellInstruction {
    @Override
    public SerializedSpellInstruction asSerialized() {
        return new SerializedSpellInstruction(SpellInstructionType.ENTER_SCOPE, null);
    }
}