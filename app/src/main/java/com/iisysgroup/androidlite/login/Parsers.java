package com.iisysgroup.androidlite.login;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Agbede on 3/26/2018.
 */

public class Parsers {
    public static final VasResult getTamsResult(String responseString) {
        VasResult result = new VasResult();

        try {
            String resultStr = "", message = "", balance = "",
                    macrosTid = "", key = "", commission = "";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(responseString.trim()));

            Document document = docBuilder.parse(is);
            Element parentElement = document.getDocumentElement();


            if (responseString.contains("errmsg"))
            {
                Element em = (Element) parentElement.getElementsByTagName("errcode").item(0);
                resultStr = em.getTextContent();

                em = (Element) parentElement.getElementsByTagName("errmsg").item(0);
                message = em.getTextContent();

                result.message = message;
                result.result = VasResult.Result.DECLINED;
            } else {
                NodeList nodeList = parentElement.getElementsByTagName("tran");

                Element tranElement = (Element) nodeList.item(0);

                NodeList tranNodeList = tranElement.getChildNodes();

                for (int i = 0; i < tranNodeList.getLength(); i++) {
                    Element node = (Element) tranNodeList.item(i);
                    if (node.getNodeName().equals("result")) {
                        resultStr = node.getTextContent();
                    }

                    if (node.getNodeName().equals("message")) {
                        message = node.getTextContent();
                    }

                    if (node.getNodeName().equals("balance")) {
                        balance = node.getTextContent();
                    }

                    if (node.getNodeName().equals("macros_tid")) {
                        macrosTid = node.getTextContent();
                    }


                    if (node.getNodeName().equals("key")) {
                        key = node.getTextContent();
                    }

                    if (node.getNodeName().equals("commission")){
                        commission = node.getTextContent();
                        result.commission = commission;
                    }

                    if (resultStr.equals("0")) {
                        result.result = VasResult.Result.APPROVED;
                        result.message = message;
                        result.balance = balance;
                        result.macrosTID = macrosTid;
                        result.key = key;
                    } else {
                        result.result = VasResult.Result.DECLINED;
                        result.message = message;
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            result.message = "An error has occurred. Please try again later. This is a parse issue";
        } catch (IOException e) {
            e.printStackTrace();
            result.message = "Server communication error. Please try again later.";
        } finally {
        }

        return result;
    }
}
