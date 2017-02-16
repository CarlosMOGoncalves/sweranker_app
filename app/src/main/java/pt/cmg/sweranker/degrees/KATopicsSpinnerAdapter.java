package pt.cmg.sweranker.degrees;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

public class KATopicsSpinnerAdapter extends BaseAdapter {

    private OnKATopicsSpinnerAdapterListener _listener;
    private Context _context;
    private List<KnowledgeArea> _knowledgeAreas;

    private int[] _knowledgeAreaViewPositions;
    private KnowledgeArea[] _knowledgeAreasAsArray;
    private KnowledgeAreaTopic[] _kaTopicsAsArray;

    private int _totalItemCount;

    public KATopicsSpinnerAdapter(Context context, List<KnowledgeArea> knowledgeAreas, OnKATopicsSpinnerAdapterListener activity) {
        _context = context;
        _listener = activity;
        _knowledgeAreas = knowledgeAreas;
        _totalItemCount = calculateItemCount(_knowledgeAreas);

        _knowledgeAreaViewPositions = calculateKnowldgeAreasViewPositions();
        _knowledgeAreasAsArray = getKnowledgeAreasAsArray();
    }


    /**
     * Basically calculates the number of Knowledge Areas plus the total combined of each Knowledge Area's topics.
     *
     * @param kas
     * @return
     */
    private int calculateItemCount(List<KnowledgeArea> kas) {

        int total = 0;
        for (KnowledgeArea ka : kas) {
            total += ka.getTopicsCount();
        }
        total += kas.size();
        return total;
    }


    /**
     * Returns an array where each position is the position where a Knowledge Area sits between all the KATopics view holders.
     * For example the array [0, 7, 10, 20, 30] means that on those positions in the recycler view I have to insert
     * a KnowledgeAreaHolder because between them I have the actual topics.
     *
     * @return
     */
    private int[] calculateKnowldgeAreasViewPositions() {
        int[] positions = new int[_knowledgeAreas.size()];

        int counter = 1;
        positions[0] = 0;
        for (int i = 1, ka = 0; i < positions.length; i++, ka++) {
            positions[i] = counter + _knowledgeAreas.get(ka).getTopicsCount();
            counter += _knowledgeAreas.get(ka).getTopicsCount() + 1;
        }

        return positions;
    }


    /**
     * This one is very tricky.
     * Returns an array where each position is occupied by the DegreeClass that matches that same position in the adapter.
     * So there is an array with the number of classes plus the number of years where only the classes are actually filled.
     * Like this [ null , DegreeClass1, DC2, DC3 , null , DC4 , ...] where each null is actually an empty spot to sit the Year View.
     *
     * @return
     */
    private KnowledgeArea[] getKnowledgeAreasAsArray() {
        KnowledgeArea[] knowledgeAreas = new KnowledgeArea[_totalItemCount];


        for (int i = 0; i < _knowledgeAreaViewPositions.length; i++) {
            knowledgeAreas[_knowledgeAreaViewPositions[i]] = _knowledgeAreas.get(i);
        }

        return knowledgeAreas;
    }


    @Override
    public int getCount() {
        return _totalItemCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }


    public interface OnKATopicsSpinnerAdapterListener {

    }
}
