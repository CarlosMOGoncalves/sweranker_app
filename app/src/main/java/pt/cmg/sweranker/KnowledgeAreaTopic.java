package pt.cmg.sweranker;

/**
 * Created by Carlos on 19/12/2016.
 */

public class KnowledgeAreaTopic {

    private String _name;
    private String _description;


    public KnowledgeAreaTopic() {
        _name = "";
        _description = "";
    }

    public KnowledgeAreaTopic(String name, String description) {
        _name = name;
        _description = description;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }
}
