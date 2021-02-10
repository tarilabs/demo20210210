package org.drools;

import static org.kie.dmn.validation.DMNValidator.Validation.*;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import org.acme.GenerateDMNXml;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.validation.DMNValidatorFactory;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateTest {
    static final Logger LOG = LoggerFactory.getLogger(GenerateTest.class);

    @Test
    public void test() {
        final String xmlUT = GenerateDMNXml.generateWith("Hello");

        List<DMNMessage> validateMsgs = DMNValidatorFactory.newValidator().validate(new StringReader(xmlUT), VALIDATE_SCHEMA, VALIDATE_MODEL);
        Assertions.assertThat(validateMsgs).isEmpty();

        final KieServices ks = KieServices.Factory.get();
        final KieHelper helper = new KieHelper();
        helper.setReleaseId(ks.newReleaseId("org.kie", "dmn-test-" + UUID.randomUUID(), "1.0"))
            .addResource(ks.getResources()
                .newByteArrayResource(xmlUT.getBytes())
                .setTargetPath("src/main/resources/demo.dmn"));
        final KieContainer kieContainer = helper.getKieContainer();
        Results verifyResults = kieContainer.verify();
        for (Message m : verifyResults.getMessages()) {
            LOG.info("{}", m);
        }
        
        final DMNRuntime dmnRuntime = KieRuntimeFactory.of(kieContainer.getKieBase()).get(DMNRuntime.class);
        final DMNModel model = dmnRuntime.getModels().get(0);
        final DMNContext context = dmnRuntime.newContext();
        context.set("Name", "John Doe");

        final DMNResult evaluateAll = dmnRuntime.evaluateAll(model, context);
        LOG.info("{}", evaluateAll);
        Assertions.assertThat(evaluateAll.getDecisionResultByName("Greetings").getResult()).isEqualTo("Hello, John Doe");
    }

    @Test
    public void testCiao() {
        final String xmlUT = GenerateDMNXml.generateWith("Ciao");

        final KieServices ks = KieServices.Factory.get();
        final KieHelper helper = new KieHelper();
        helper.setReleaseId(ks.newReleaseId("org.kie", "dmn-test-" + UUID.randomUUID(), "1.0"))
            .addResource(ks.getResources()
                .newByteArrayResource(xmlUT.getBytes())
                .setTargetPath("src/main/resources/demo.dmn"));
        final KieContainer kieContainer = helper.getKieContainer();
        Results verifyResults = kieContainer.verify();
        for (Message m : verifyResults.getMessages()) {
            LOG.info("{}", m);
        }
        
        final DMNRuntime dmnRuntime = KieRuntimeFactory.of(kieContainer.getKieBase()).get(DMNRuntime.class);
        final DMNModel model = dmnRuntime.getModels().get(0);
        final DMNContext context = dmnRuntime.newContext();
        context.set("Name", "John Doe");

        final DMNResult evaluateAll = dmnRuntime.evaluateAll(model, context);
        LOG.info("{}", evaluateAll);
        Assertions.assertThat(evaluateAll.getDecisionResultByName("Greetings").getResult()).isEqualTo("Ciao, John Doe");
    }
}