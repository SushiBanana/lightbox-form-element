package org.joget.sample;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.FileUpload;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.FileManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Lightbox extends FileUpload {
    public static String MESSAGE_PATH = "message/lightbox";
    public static String PROPERTIES_PATH = "/properties/lightbox.json";

    @Override
    public String getName(){
        return "Lightbox File Upload";
    }

    @Override
    public String getVersion(){
        return "8.1.8";
    }

    @Override
    public String getDescription() {
        return "Lightbox File Upload Element";
    }

    @Override
    public String getFormBuilderCategory(){
        return FormBuilderPalette.CATEGORY_CUSTOM;
    }

    @Override
    public int getFormBuilderPosition(){
        return 1;
    }

    @Override
    public String getFormBuilderIcon(){
        return "<i class=\"fas fa-file-arrow-up\"></i>";
    }

    @Override
    public String getFormBuilderTemplate(){
        return "<label class='label'>Lightbox File Upload</label><input type='file' />";
    }

    @Override
    public String getLabel(){
        return AppPluginUtil.getMessage(getClassName() + ".label", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions(){
        return AppUtil.readPluginResource(getClassName(), PROPERTIES_PATH, null, true, MESSAGE_PATH);
    }

    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "lightbox.ftl";

        // set value
        String[] values = FormUtil.getElementPropertyValues(this, formData);

        Map<String, String> tempFilePaths = new LinkedHashMap<String, String>();
        Map<String, String> filePaths = new LinkedHashMap<String, String>();

        String primaryKeyValue = getPrimaryKeyValue(formData);
        String filePathPostfix = "_path";
        String id = FormUtil.getElementParameterName(this);

        //check is there a stored value
        String storedValue = formData.getStoreBinderDataProperty(this);
        if (storedValue != null) {
            values = storedValue.split(";");
        } else {
            //if there is no stored value, get the temp files
            String[] tempExisting = formData.getRequestParameterValues(id + filePathPostfix);

            if (tempExisting != null && tempExisting.length > 0) {
                values = tempExisting;
            }
        }

        String formDefId = "";
        Form form = FormUtil.findRootForm(this);
        if (form != null) {
            formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
        }
        String appId = "";
        String appVersion = "";

        AppDefinition appDef = AppUtil.getCurrentAppDefinition();

        if (appDef != null) {
            appId = appDef.getId();
            appVersion = appDef.getVersion().toString();
        }

        for (String value : values) {
            // check if the file is in temp file
            File file = FileManager.getFileByPath(value);

            if (file != null) {
                tempFilePaths.put(value, file.getName());
            } else if (value != null && !value.isEmpty()) {
                // determine actual path for the file uploads
                String fileName = value;
                String encodedFileName = fileName;
                if (fileName != null) {
                    try {
                        encodedFileName = URLEncoder.encode(fileName, "UTF8").replaceAll("\\+", "%20");
                    } catch (UnsupportedEncodingException ex) {
                        // ignore
                    }
                }

                String filePath = "/web/client/app/" + appId + "/" + appVersion + "/form/download/" + formDefId + "/" + primaryKeyValue + "/" + encodedFileName + ".";
                if (Boolean.valueOf(getPropertyString("attachment")).booleanValue()) {
                    filePath += "?attachment=true";
                }
                filePaths.put(filePath, value);
            }
        }

        if (!tempFilePaths.isEmpty()) {
            dataModel.put("tempFilePaths", tempFilePaths);
        }
        if (!filePaths.isEmpty()) {
            dataModel.put("filePaths", filePaths);
        }

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }




}
