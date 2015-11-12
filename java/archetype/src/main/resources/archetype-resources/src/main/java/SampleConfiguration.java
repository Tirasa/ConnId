#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public class SampleConfiguration extends AbstractConfiguration {

    private String sampleProperty = "SAMPLE VALUE";

    @ConfigurationProperty(displayMessageKey = "sampleProperty.display",
            helpMessageKey = "sampleProperty.help", order = 1)
    public String getSampleProperty() {
        return sampleProperty;
    }

    public void setSampleProperty(String sampleProperty) {
        this.sampleProperty = sampleProperty;
    }

    @Override
    public void validate() {
        if (StringUtil.isBlank(sampleProperty)) {
            throw new ConfigurationException("sampleProperty must not be blank!");
        }
    }

}
