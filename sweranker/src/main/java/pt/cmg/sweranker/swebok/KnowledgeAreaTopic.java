package pt.cmg.sweranker.swebok;

import pt.cmg.sweranker.R;


public class KnowledgeAreaTopic {

    private int _nameResource;
    private int _descriptionResource;
    private int _id;
    private int _colorResource;
    private int _knowledgeAreaId;


    public KnowledgeAreaTopic() {
    }

    public KnowledgeAreaTopic(int id, int knowledgeAreaId) {
        _id = id;
        _knowledgeAreaId = knowledgeAreaId;
    }

    public KnowledgeAreaTopic(int knowledgeAreaId) {
        _knowledgeAreaId = knowledgeAreaId;
    }

    public KnowledgeAreaTopic(int nameResource, int descriptionResource, int id) {
        _nameResource = nameResource;
        _descriptionResource = descriptionResource;
        _id = id;
    }

    public int getNameResource() {
        return _nameResource;
    }

    public void setNameResource(int nameResource) {
        _nameResource = nameResource;
    }

    public int getDescriptionResource() {
        return _descriptionResource;
    }

    public void setDescriptionResource(int descriptionResource) {
        _descriptionResource = descriptionResource;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public void setKnowledgeAreaId(int kaId) {
        _knowledgeAreaId = kaId;
    }

    public int getKnowledgeAreaId() {
        return _knowledgeAreaId;
    }

    public int getColorResource() {
        if (_colorResource == 0) {
            return R.color.textColor;
        }
        return _colorResource;
    }

    public void setColorResource(int colorResource) {
        _colorResource = colorResource;
    }
}
