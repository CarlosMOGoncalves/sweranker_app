package pt.cmg.sweranker.degrees;


import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

public class KATopicsSpinnerAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_TAG_KEY = 1;
    private static final int VIEW_HOLDER_TAG_KEY = 2;
    private static final int VIEW_TYPE_KA = 10;
    private static final int VIEW_TYPE_TOPIC = 20;

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
        _kaTopicsAsArray = getKATopicsAsArray();
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
        // Because this is freaking Spinner it has a View on position ZERO, the one before the dropdown menu appears...
//        total++;

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

        // Again, because this is a Spinner the position 0 will be empty (or with selected View)
        // and 1 will be the first KA View, so we skip 2.
        int skipViewOffset = 1;
        positions[0] = 0;
        for (int i = 1, ka = 0; i < positions.length; i++, ka++) {

            positions[i] = skipViewOffset + _knowledgeAreas.get(ka).getTopicsCount();
            // Here we skip another KA View, that's where the +1 comes from
            skipViewOffset += _knowledgeAreas.get(ka).getTopicsCount() + 1;
        }

        return positions;
    }


    /**
     * This one is very tricky.
     * Returns an array where each position is occupied by the KnowledgeArea that matches that same position in the adapter.
     * So there is an array with the number of classes plus the number of years where only the classes are actually filled.
     * Like this [ null , DegreeClass1, DC2, DC3 , null , DC4 , ...] where each null is actually an empty spot to sit the Year View.
     *
     * @return
     */
    private KnowledgeArea[] getKnowledgeAreasAsArray() {
        KnowledgeArea[] knowledgeAreasSparcedArray = new KnowledgeArea[_totalItemCount];

        for (int i = 0; i < _knowledgeAreaViewPositions.length; i++) {
            // Again, the plus one is accounting for the empty view on position 0... this is sad code...
            knowledgeAreasSparcedArray[_knowledgeAreaViewPositions[i]] = _knowledgeAreas.get(i);
        }

        return knowledgeAreasSparcedArray;
    }


    /**
     * This one is very tricky.
     * Returns an array where each position is occupied by the KnowledgeAreaTopic that matches that same position in the adapter.
     * So there is an array with the number of topics plus the number of KAs where only the topics are actually filled.
     * Like this [ null , KnowledgeAreaTopic1, KnowledgeAreaTopic2, KnowledgeAreaTopic3 , null , KnowledgeAreaTopic4 , ...]
     * where each null is actually an empty spot to sit the KnowledgeArea View.
     *
     * @return
     */
    private KnowledgeAreaTopic[] getKATopicsAsArray() {
        KnowledgeAreaTopic[] kaTopicsSparsedArray = new KnowledgeAreaTopic[_totalItemCount];

        // This will iterate over ALL of the available positions in the array that was allocated
        // AND again we account for the top TWO views, the empty one and the first KA View
        for (int i = 1; i < kaTopicsSparsedArray.length; i++) {

            // And then for each KA
            for (KnowledgeArea knowledgeArea : _knowledgeAreas) {
                // And for each topic, we just add it the position
                for (KnowledgeAreaTopic topic : knowledgeArea.getTopics()) {
                    kaTopicsSparsedArray[i] = topic;
                    i++;
                }
                // account for the year view
                i++;
            }

        }
        return kaTopicsSparsedArray;
    }


    @Override
    public int getCount() {
        return _totalItemCount;
    }

    @Override
    public Object getItem(int position) {
        boolean isKAView = Arrays.binarySearch(_knowledgeAreaViewPositions, position) > 0 ? true : false;

        if (isKAView) {
            return _knowledgeAreasAsArray[position];
        } else {
            return _kaTopicsAsArray[position];
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        KnowledgeAreaViewHolder kaViewHolder = null;
        KnowledgeAreaTopicViewHolder topicViewHolder = null;

        if (convertView == null || (Integer) convertView.getTag(R.id.view_type_tag) != getItemViewType(position)) {

            if (getItemViewType(position) == VIEW_TYPE_KA) {
                // inflate the layout
                LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.degree_matcher_spinner_ka_item, parent, false);

                // well set up the ViewHolder
                kaViewHolder = new KnowledgeAreaViewHolder(convertView);

                convertView.setTag(R.id.view_type_tag, getItemViewType(position));
                convertView.setTag(R.id.view_holder_tag, kaViewHolder);

            } else {
                // inflate the layout
                LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.degree_matcher_spinner_topic_item, parent, false);

                // well set up the ViewHolder
                topicViewHolder = new KnowledgeAreaTopicViewHolder(convertView);

                convertView.setTag(R.id.view_type_tag, getItemViewType(position));
                convertView.setTag(R.id.view_holder_tag, topicViewHolder);

            }


        } else {
            if (getItemViewType(position) == VIEW_TYPE_KA) {
                kaViewHolder = (KnowledgeAreaViewHolder) convertView.getTag(R.id.view_holder_tag);
            } else {
                topicViewHolder = (KnowledgeAreaTopicViewHolder) convertView.getTag(R.id.view_holder_tag);
            }
        }

        if (getItemViewType(position) == VIEW_TYPE_KA) {
            kaViewHolder._kaName.setText(_context.getString(_knowledgeAreasAsArray[position].getNameResource()));
            kaViewHolder._kaName.setTextColor(ContextCompat.getColor(_context, _knowledgeAreasAsArray[position].getColourResource()));

        } else {

            topicViewHolder._topicName.setText(_context.getString(_kaTopicsAsArray[position].getNameResource()));
            topicViewHolder._helpButton.setColorFilter(ContextCompat.getColor(_context, R.color.colorPrimary));
            topicViewHolder._helpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(_context, "Allahu akbar", Toast.LENGTH_SHORT).show();
                }
            });

        }
        return convertView;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // inflate the layout
        LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.degree_matcher_spinner_selected_item, parent, false);

        // well set up the ViewHolder
        SelectedTopicViewHolder viewHolder = new SelectedTopicViewHolder(convertView);
        viewHolder._selectedTopic.setText("(none selected)");

        // store the holder with the view.
//        convertView.setTag(viewHolder);


        return convertView;

    }

    @Override
    public int getItemViewType(int position) {

        boolean isKAView = Arrays.binarySearch(_knowledgeAreaViewPositions, position) >= 0;

        if (isKAView) {
            return VIEW_TYPE_KA;
        } else {
            return VIEW_TYPE_TOPIC;
        }
    }

    /**
     * Really just a marker class to be able to inflate the textview
     */
    static class KnowledgeAreaViewHolder {

        private TextView _kaName;

        public KnowledgeAreaViewHolder(View rootView) {
            _kaName = (TextView) rootView.findViewById(R.id.ka_name);
        }
    }

    /**
     * ViewHolder pattern to hold one of the cards
     */
    static class KnowledgeAreaTopicViewHolder {

        private TextView _topicName;
        private ImageButton _helpButton;

        public KnowledgeAreaTopicViewHolder(View rootView) {
            _topicName = (TextView) rootView.findViewById(R.id.topic_name);
            _helpButton = (ImageButton) rootView.findViewById(R.id.info_button);
        }
    }

    static class SelectedTopicViewHolder {
        private TextView _selectedTopic;

        public SelectedTopicViewHolder(View rootView) {
            _selectedTopic = (TextView) rootView.findViewById(R.id.selected_topic);
        }
    }


    public interface OnKATopicsSpinnerAdapterListener {

    }
}
