package pt.cmg.sweranker;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeArea {

    private int _nameResource;
    private int _imageResource;
    private int _descriptionResource;
    private int _id;
    private List<KnowledgeAreaTopic> _topics;

    public KnowledgeArea() {
        _nameResource = 0;
        _imageResource = 0;
        _descriptionResource = 0;
        _topics = new ArrayList<>();
        _id = 0;

    }

    public KnowledgeArea(int nameResource, int imageResource, int descriptionResource, int id) {
        _nameResource = nameResource;
        _imageResource = imageResource;
        _descriptionResource = descriptionResource;
        _topics = new ArrayList<>();
        _id = id;
    }

    public void setNameResource(int nameResource) {
        _nameResource = nameResource;
    }

    public void setDescriptionResource(int descriptionResource) {
        _descriptionResource = descriptionResource;
    }

    public void setImageResource(int imageResource) {
        _imageResource = imageResource;
    }

    public int getDescriptionResource() {
        return _descriptionResource;
    }

    public int getNameResourec() {
        return _nameResource;
    }

    public int getImageResource() {
        return _imageResource;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public List<KnowledgeAreaTopic> getTopics() {
        return _topics;
    }

    public int getTopicsCount() {
        return _topics.size();
    }

    public void setTopics(List<KnowledgeAreaTopic> topics) {
        _topics = topics;
    }

    public void addTopic(KnowledgeAreaTopic topic) {
        _topics.add(topic);
    }
}


