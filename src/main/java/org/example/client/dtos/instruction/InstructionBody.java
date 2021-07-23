package org.example.client.dtos.instruction;

public class InstructionBody {
    private final String instructionType;
    private final Long amount;

    public InstructionBody(String instructionType) {
        this.instructionType = instructionType;
        this.amount = null;
    }

    public InstructionBody(String instructionType, long amount) {
        this.instructionType = instructionType;
        this.amount = amount;
    }

    public String getInstructionType() {
        return instructionType;
    }

    public long getAmount() {
        return amount;
    }
}
