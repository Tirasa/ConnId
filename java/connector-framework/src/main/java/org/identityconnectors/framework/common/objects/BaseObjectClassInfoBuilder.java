package org.identityconnectors.framework.common.objects;

public abstract class BaseObjectClassInfoBuilder<T extends BaseObjectClassInfoBuilder,
        O extends LightweightObjectClassInfo> {

    protected boolean isContainer;

    protected boolean isAuxiliary;

    protected boolean isEmbedded;

    protected String type;

    public BaseObjectClassInfoBuilder() {
        type = ObjectClass.ACCOUNT_NAME;
    }

    /**
     * Sets the specified {@link ObjectClassInfo#getType() type} for the
     * {@link ObjectClassInfo} object that is being built.
     *
     * (If this method is not called, the <code>ObjectClassInfo</code> that is
     * being built will default to {@link ObjectClass#ACCOUNT_NAME} -- that is,
     * its <code>type</code> will default to to a String value of
     * {@link ObjectClass#ACCOUNT_NAME}.)
     *
     * @see ObjectClassInfo#getType()
     * @see ObjectClass#ACCOUNT_NAME
     */
    public T setType(final String type) {
        this.type = type;
        return getThis();
    }

    /**
     * Set to true to indicate this is a container type.
     *
     * @param container True if this is a container type.
     */
    public void setContainer(final boolean container) {
        isContainer = container;
    }

    public void setAuxiliary(final boolean isAuxiliary) {
        this.isAuxiliary = isAuxiliary;
    }

    public T setEmbedded(final boolean embedded) {

        isEmbedded = embedded;
        return getThis();
    }

    protected abstract T getThis();

    /**
     * Constructs an instance of an object which extends the {@link LightweightObjectClassInfo} class with any
     * characteristics that were previously specified using this builder.
     *
     * @return an instance of an object which extends {@link LightweightObjectClassInfo} with the characteristics
     * previously specified.
     */
    public abstract O build();
}
