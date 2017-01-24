package pt.cmg.sweranker.knowledgeareas;

/**
 * Created by Carlos on 19/12/2016.
 */

public class KnowledgeAreaTopic {

    private int _nameResource;
    private int _descriptionResource;
    private int _id;


    public KnowledgeAreaTopic() {
        _nameResource = 0;
        _descriptionResource = 0;
        _id = 0;
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
}
