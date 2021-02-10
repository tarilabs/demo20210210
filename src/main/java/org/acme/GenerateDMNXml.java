package org.acme;

import java.util.Map;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.model.api.*;
import org.kie.dmn.model.v1_3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateDMNXml {

    static final Logger LOG = LoggerFactory.getLogger(GenerateDMNXml.class);
    
    public static String generateWith(String prefix) {
        Definitions definitions = new TDefinitions();
        setDefaultNSContext(definitions);
        definitions.setName("demo20210210");
        String namespace = "demo20210210_" + UUID.randomUUID();
        definitions.setNamespace(namespace);
        definitions.getNsContext().put(XMLConstants.DEFAULT_NS_PREFIX, namespace);
        definitions.setExporter("demo20210210");

        // InputData node
        InputData id = new TInputData();
        final String NAME = "Name";
        id.setName(NAME);
        id.setId("id_"+NAME);
        InformationItem idVar = new TInformationItem();
        idVar.setName(NAME);
        idVar.setId("idvar_"+NAME);
        idVar.setTypeRef(new QName("string")); // WARN the kie-dmn-model for retro compatibility to DMNv1.1 still accept this, but might be subject of future refactorings.
        id.setVariable(idVar);
        
        // BKM node
        BusinessKnowledgeModel bkm = new TBusinessKnowledgeModel();
        final String PREFIXER = "Prefixer";
        bkm.setName(PREFIXER);
        bkm.setId("id_"+PREFIXER);
        InformationItem bVar = new TInformationItem();
        bVar.setName(PREFIXER);
        bVar.setId("idvar_"+PREFIXER);
        bVar.setTypeRef(new QName("string"));
        bkm.setVariable(bVar);
        FunctionDefinition bkmDL = new TFunctionDefinition();
        InformationItem p1 = new TInformationItem();
        final String P1 = "p1";
        p1.setName(P1);
        p1.setTypeRef(new QName("string")); // WARN the kie-dmn-model for retro compatibility to DMNv1.1 still accept this but might be subject of future refactorings.
        bkmDL.getFormalParameter().add(p1);
        LiteralExpression le = new TLiteralExpression();
        le.setText("\""+prefix+", \"+p1"); // demonstrates programmatically defines Decision Logic inside DMN.
        bkmDL.setExpression(le);
        bkm.setEncapsulatedLogic(bkmDL);

        // Decision node
        Decision decision = new TDecision();
        final String GREETINGS = "Greetings";
        decision.setName(GREETINGS);
        decision.setId("id_"+GREETINGS);
        InformationItem dVar = new TInformationItem();
        dVar.setName(GREETINGS);
        dVar.setId("idvar_"+GREETINGS);
        dVar.setTypeRef(new QName("string"));
        decision.setVariable(dVar);
        InformationRequirement depOnID = new TInformationRequirement();
        depOnID.setId("ir_"+NAME);
        DMNElementReference depOnIDER = new TDMNElementReference();
        depOnIDER.setHref("#"+id.getId());
        depOnID.setRequiredInput(depOnIDER);
        decision.getInformationRequirement().add(depOnID);
        KnowledgeRequirement depOnBKM = new TKnowledgeRequirement();
        depOnBKM.setId("kr_"+PREFIXER);
        DMNElementReference depOnBKMER = new TDMNElementReference();
        depOnBKMER.setHref("#"+bkm.getId());
        depOnBKM.setRequiredKnowledge(depOnBKMER);
        decision.getKnowledgeRequirement().add(depOnBKM);
        Invocation dE = new TInvocation();
        LiteralExpression dEE = new TLiteralExpression();
        dEE.setText(PREFIXER);
        TBinding b1 = new TBinding();
        InformationItem b1p1 = new TInformationItem();
        b1p1.setName(P1);
        b1p1.setTypeRef(new QName("string")); 
        b1.setParameter(b1p1);
        LiteralExpression b1e = new TLiteralExpression();
        b1e.setText(NAME);
        b1.setExpression(b1e);
        dE.getBinding().add(b1);
        dE.setExpression(dEE);
        decision.setExpression(dE);

        definitions.getDrgElement().add(id);
        definitions.getDrgElement().add(decision);
        definitions.getDrgElement().add(bkm);

        DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
        String xml = dmnMarshaller.marshal(definitions);
        LOG.info("Generated XML:\n{}", xml);
        return xml;
    }

    private static void setDefaultNSContext(Definitions definitions) {
        Map<String, String> nsContext = definitions.getNsContext();
        nsContext.put("feel", KieDMNModelInstrumentedBase.URI_FEEL);
        nsContext.put("dmn", KieDMNModelInstrumentedBase.URI_DMN);
        nsContext.put("dmndi", KieDMNModelInstrumentedBase.URI_DMNDI);
        nsContext.put("di", KieDMNModelInstrumentedBase.URI_DI);
        nsContext.put("dc", KieDMNModelInstrumentedBase.URI_DC);
    }

    public static void main(String[] args) {
        generateWith("Hello");
    }
}
