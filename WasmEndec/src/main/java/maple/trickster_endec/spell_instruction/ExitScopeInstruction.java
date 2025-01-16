package maple.trickster_endec.spell_instruction;

public class ExitScopeInstruction implements SpellInstruction {
    @Override
    public SerializedSpellInstruction asSerialized() {
        return new SerializedSpellInstruction(SpellInstructionType.EXIT_SCOPE, null);
    }
}