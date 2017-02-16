package pt.cmg.sweranker.swebok;

import java.util.List;

/**
 * Created by Carlos on 15/02/2017.
 */

public interface KnowledgeAreaLoader {

    /**
     * Returns the list of Knowledge Areas from the system
     *
     * @return
     */
    List<KnowledgeArea> getKnowledgeAreas();


    /**
     * Returns a specific Knowledge Area from the system.
     *
     * @param knowledgeAreaIdToLoad
     * @return
     */
    KnowledgeArea getKnowledgeArea(int knowledgeAreaIdToLoad);


}
