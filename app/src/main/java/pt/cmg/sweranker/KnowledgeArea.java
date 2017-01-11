package pt.cmg.sweranker;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeArea {

    private int _nameResource;
    private int _image;
    private String _description;
    private List<KnowledgeAreaTopic> _topics;

    public KnowledgeArea() {
        _nameResource = 0;
        _image = 0;
        _description = "";
        _topics = new ArrayList<>();

    }

    public KnowledgeArea(int nameResource, int image, String description) {
        _nameResource = nameResource;
        _image = image;
        _description = description;
        _topics = new ArrayList<>();
    }

    public void setName(int nameResource) {
        _nameResource = nameResource;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public void setImage(int image) {
        _image = image;
    }

    public String getDescription() {
        return _description;
    }

    public int getName() {
        return _nameResource;
    }

    public int getImage() {
        return _image;
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
