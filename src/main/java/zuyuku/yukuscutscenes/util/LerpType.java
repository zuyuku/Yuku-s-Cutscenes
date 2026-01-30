package zuyuku.yukuscutscenes.util;

public enum LerpType implements LerpOperation<Float>{
    LINEAR(x -> x),

    EASE_IN_SINE(x -> (float)(1 - Math.cos((x * Math.PI) / 2))),
    EASE_OUT_SINE(x ->(float)(Math.sin((x * Math.PI) / 2))),
    EASE_IN_OUT_SINE(x -> (float)(-(Math.cos(Math.PI * x) - 1) / 2)),

    EASE_IN_QUAD(x -> x * x),
    EASE_OUT_QUAD(x -> 1 - (1 - x) * (1 - x)),
    EASE_IN_OUT_QUAD(x -> (float)(x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2)),
    
    EASE_IN_CUBIC(x -> x * x * x),
    EASE_OUT_CUBIC(x -> (float)(1 - Math.pow(1 - x, 3))),
    EASE_IN_OUT_CUBIC(x -> (float)(x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2)),
    
    EASE_IN_QUART(x -> x * x * x * x),
    EASE_OUT_QUART(x -> (float)(1 - Math.pow(1 - x, 4))),
    EASE_IN_OUT_QUART(x -> (float)(x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2)),
    
    EASE_IN_QUINT(x -> x * x * x * x * x),
    EASE_OUT_QUINT(x -> (float)(1 - Math.pow(1 - x, 5))),
    EASE_IN_OUT_QUINT(x -> (float)(x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2));

    private final LerpOperation<Float> lerpOperation;

    public static LerpType fromString(String name) {
        for(LerpType lerpType : values())
            if(lerpType.name().matches(name))
                return lerpType;
        return null;
    }

    LerpType(final LerpOperation<Float> lerpOperation) {
        this.lerpOperation = lerpOperation;
    }

    @Override
    public Float compute(Float t) {
        return lerpOperation.compute(t);
    }

}
