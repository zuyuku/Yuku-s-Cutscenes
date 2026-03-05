package zuyuku.yukuscutscenes.util;

public enum LerpType implements LerpOperation<Float>{
    LINEAR(x -> x),

    SINE_IN(x -> (float)(1 - Math.cos((x * Math.PI) / 2))),
    SINE_OUT(x ->(float)(Math.sin((x * Math.PI) / 2))),
    SINE_IN_OUT(x -> (float)(-(Math.cos(Math.PI * x) - 1) / 2)),
    
    CUBIC_IN(x -> x * x * x),
    CUBIC_OUT(x -> (float)(1 - Math.pow(1 - x, 3))),
    CUBIC_IN_OUT(x -> (float)(x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2)),
    
    QUINT_IN(x -> x * x * x * x * x),
    QUINT_OUT(x -> (float)(1 - Math.pow(1 - x, 5))),
    QUINT_IN_OUT(x -> (float)(x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2)),

    BOUNCE_OUT(x -> {float n1 = 7.5625f;
        float d1 = 2.75f;
        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            return n1 * (x -= 1.5f / d1) * x + 0.75f;
        } else if (x < 2.5 / d1) {
            return n1 * (x -= 2.25f / d1) * x + 0.9375f;
        } else {
            return n1 * (x -= 2.625f / d1) * x + 0.984375f;
        }
    }),
    BOUNCE_IN(x -> 1-BOUNCE_OUT.compute(1-x)),
    BOUNCE_IN_OUT(x -> x < 0.5? (1 - BOUNCE_OUT.compute(1 - 2 * x)) / 2: (1 + BOUNCE_OUT.compute(2 * x - 1)) / 2);

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
