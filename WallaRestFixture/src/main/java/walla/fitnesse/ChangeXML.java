package walla.fitnesse;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ChangeXML {

	public static final boolean DEFAULT_FAIL_ON_ERROR = true;

	private String outputXml;
	private Element root;
	
	public ChangeXML(String shellXml, String xmlUpdateAttribute, String dataXml, String dataToRetrieveXPath) throws StopTestException 
	{
		//Retrieve resultset and check for some value being returned.
		ArrayList<String> results = RetrieveArrayFromXml(dataXml, dataToRetrieveXPath);
		if (results.size() < 1)
			return;
		
		//Find node to replace
		Document xmlDocument = loadXMLFromString(shellXml);

		//Find node to replace
		XPath nodeToUpdateXPath = XPathFactory.newInstance().newXPath();
		Node toReplaceNode = null;
		try {
			toReplaceNode = (Node)nodeToUpdateXPath.evaluate("//*[@" + xmlUpdateAttribute + "='#WALLAIDLOOP#']", xmlDocument.getDocumentElement(), XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new StopTestException("#WALLAIDLOOP# failed to be found.  Error was: " + e.getMessage());
		}
		
		if (toReplaceNode == null)
			throw new StopTestException("Changed XML failed, as one unique element must be returned for the tag name.");

		//String attributeName = attributeNode.getNodeName();
		
		//Node toReplaceNode = attributeNode.getParentNode();
		
		//Update first node outside of loop
		NamedNodeMap nodeMap = toReplaceNode.getAttributes();
		nodeMap.getNamedItem(xmlUpdateAttribute).setNodeValue(results.get(0));
		
		//Update node with the replaced value
		//For each result add sibling to xml document with the correct id.
		for (int i = 1; i < results.size(); i++)
		{
			Node cloned = toReplaceNode.cloneNode(true);
			NamedNodeMap cloneNodeMap = cloned.getAttributes();
			cloneNodeMap.getNamedItem(xmlUpdateAttribute).setNodeValue(results.get(i));
			toReplaceNode.getParentNode().appendChild(cloned);
		}

		outputXml = "";
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(xmlDocument);
			transformer.transform(source, result);
			outputXml = result.getWriter().toString();
		} 
		catch(TransformerException ex) {
			throw new StopTestException("Updating XML failed, String could not be generated.  Error: " + ex.getMessage());
		}
	}
	

	private ArrayList<String> RetrieveArrayFromXml(String dataXml, String dataToRetrieveXPath) throws StopTestException
	{
		ArrayList<String> stringArrayList = new ArrayList<String>();
		
		Document xmlDocument = loadXMLFromString(dataXml);
		
		XPath dataNodeXPath = XPathFactory.newInstance().newXPath();
		NodeList toReadNodes = null;
		try {
			toReadNodes = (NodeList)dataNodeXPath.evaluate(dataToRetrieveXPath, xmlDocument.getDocumentElement(), XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new StopTestException("XPath evaluate failed.  Error was: " + e.getMessage());
		}
		
		for (int i = 0; i < toReadNodes.getLength(); i++)
		{
			Node current = toReadNodes.item(i);
			stringArrayList.add(current.getNodeValue());
		}
		
		return stringArrayList;
	}
	
	public String output() {
		return outputXml;
	}

	protected Element getRoot() {
		return root;
	}

	protected static Document loadXMLFromString(String xml) throws StopTestException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document document = null;
		try {
			builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			document = builder.parse(is);
		} catch (Exception e) {
			throw new StopTestException("Could not parse response with error: " + e.getMessage());
		}

		return document;
	}


}