package org.identityconnectors.framework.common.objects;

public class LightweightObjectClassInfoBuilder extends BaseObjectClassInfoBuilder<LightweightObjectClassInfoBuilder,
        LightweightObjectClassInfo> {
    @Override
    protected LightweightObjectClassInfoBuilder getThis() {
        return this;
    }

    /**
     * Constructs an instance of {@link LightweightObjectClassInfo} with any
     * characteristics that were previously specified using this builder.
     *
     * @return an instance of {@link LightweightObjectClassInfo} with the characteristics
     * previously specified.
     */
    @Override
    public LightweightObjectClassInfo build() {
        return new LightweightObjectClassInfo(
                type,
                isContainer,
                isAuxiliary,
                isEmbedded);
    }
}
