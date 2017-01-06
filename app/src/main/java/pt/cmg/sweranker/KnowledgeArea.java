package pt.cmg.sweranker;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeArea {

    private String _name;
    private int _image;
    private String _description;
    private List<KnowledgeAreaTopic> _topics;

    public KnowledgeArea() {
        _name = "";
        _image = 0;
        _description = "";
        _topics = new ArrayList<>();

    }

    public KnowledgeArea(String name, int image, String description) {
        _name = name;
        _image = image;
        _description = description;
        _topics = new ArrayList<>();
    }

    public void setName(String name) {
        _name = name;
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

    public String getName() {
        return _name;
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
