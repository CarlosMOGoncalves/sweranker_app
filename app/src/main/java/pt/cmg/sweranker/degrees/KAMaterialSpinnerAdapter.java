package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;
import pt.cmg.sweranker.ui.materialspinner.MaterialSpinnerBaseAdapter;

/**
 * This is the adapter that binds all the Knowledge Areas to a Material Spinner.
 * The idea behind it is to create a Spinner that shows all the possible KA topics
 * but separated by a KA simples view (painted with its system colour).
 * Now the topics are selectable, the KA not.
 * <p>
 * It is organized in a structure that I've used before whenever I have multiple views separated
 * but some other view -> I use native arrays with the positions of the separeted views and also
 * one array for each object type. It is neat, because accesses are easy and fast, but it is kinda
 * nerd.
 * <p>
 * Created by Carlos on 21/02/2017.
 */

public class KAMaterialSpinnerAdapter extends MaterialSpinnerBaseAdapter {

    private static final int VIEW_TYPE_KA = 10;
    private static final int VIEW_TYPE_TOPIC = 20;

    private OnKATopicsSpinnerAdapterListener _listener;
    private Context _context;
    private List<KnowledgeArea> _knowledgeAreas;

    private int[] _knowledgeAreaViewPositions;
    private KnowledgeArea[] _knowledgeAreasAsArray;
    private KnowledgeAreaTopic[] _kaTopicsAsArray;

    private int _totalItemCount;


    public KAMaterialSpinnerAdapter(Context context) {
        super(context);
    }

    public KAMaterialSpinnerAdapter(Context context, List<KnowledgeArea> knowledgeAreas, OnKATopicsSpinnerAdapterListener activity) {
        super(context);
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

        int skipViewOffset = 1;
        positions[0] = 0;
        for (int i = 1, ka = 0; i < positions.length; i++, ka++) {

            positions[i] = skipViewOffset + _knowledgeAreas.get(ka).getTopicsCount();
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
                // account for the KA view
                i++;
            }

        }
        return kaTopicsSparsedArray;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        if (viewType == VIEW_TYPE_KA) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_matcher_spinner_ka_list_item, parent, false);
            return new KnowledgeAreaViewHolder(itemView);
        }

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_matcher_spinner_topic_list_item, parent, false);
        return new KnowledgeAreaTopicViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof KnowledgeAreaViewHolder) {
            ((KnowledgeAreaViewHolder) holder)._kaName.setText(_context.getResources().getString(_knowledgeAreasAsArray[position].getNameResource()));
            ((KnowledgeAreaViewHolder) holder)._kaName.setTextColor(ContextCompat.getColor(_context, _knowledgeAreasAsArray[position].getColourResource()));
        } else {
            ((KnowledgeAreaTopicViewHolder) holder)._topicName.setText(_context.getResources().getString(_kaTopicsAsArray[position].getNameResource()));
            ((KnowledgeAreaTopicViewHolder) holder)._helpButton.setColorFilter(ContextCompat.getColor(_context, R.color.colorPrimary));
        }
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

    @Override
    public int getItemCount() {
        return _totalItemCount;
    }

    @Override
    public Object getItem(int position) {

        boolean isKAView = Arrays.binarySearch(_knowledgeAreaViewPositions, position) >= 0 ? true : false;

        if (isKAView) {
            return _knowledgeAreasAsArray[position];
        } else {
            return _kaTopicsAsArray[position];
        }
    }

    @Override
    public String getItemName(int position) {
        boolean isKAView = Arrays.binarySearch(_knowledgeAreaViewPositions, position) >= 0 ? true : false;

        if (isKAView) {
            return _context.getString(_knowledgeAreasAsArray[position].getNameResource());
        } else {
            return _context.getString(_kaTopicsAsArray[position].getNameResource());
        }
    }

    @Override
    public boolean isValidPosition(int position) {
        // Basically it is only valid if it is a topic, although I do have all the data
        return Arrays.binarySearch(_knowledgeAreaViewPositions, position) >= 0 ? false : true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public List<Object> getItems() {
        return null;
    }


    /**
     * Really just a marker class to be able to inflate the textview
     */
    public class KnowledgeAreaViewHolder extends RecyclerView.ViewHolder {

        private TextView _kaName;

        public KnowledgeAreaViewHolder(View rootView) {
            super(rootView);
            _kaName = (TextView) rootView.findViewById(R.id.ka_name);
            Configuration config = _context.getResources().getConfiguration();
            if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                _kaName.setTextDirection(View.TEXT_DIRECTION_RTL);
            }
        }
    }

    /**
     * ViewHolder pattern to hold one of the cards
     */
    private class KnowledgeAreaTopicViewHolder extends RecyclerView.ViewHolder {

        private TextView _topicName;
        private ImageButton _helpButton;

        public KnowledgeAreaTopicViewHolder(View rootView) {
            super(rootView);
            _topicName = (TextView) rootView.findViewById(R.id.ka_topic_name);
            _topicName.setOnClickListener(v -> {

                KnowledgeAreaTopic selectedTopic = _kaTopicsAsArray[getAdapterPosition()];
                if (_onItemSelectedListener != null) {
                    _onItemSelectedListener.onItemSelected(selectedTopic, _context.getString(selectedTopic.getNameResource()), ContextCompat.getColor(_context, selectedTopic.getColorResource()), getAdapterPosition());
                }
                _listener.getSelectedTopicId(selectedTopic.getId());
            });

            _helpButton = (ImageButton) rootView.findViewById(R.id.info_button);
            _helpButton.setOnClickListener(v -> {
                KnowledgeAreaTopic selectedTopic = _kaTopicsAsArray[getAdapterPosition()];
                KATopicInformationDialog dialog = KATopicInformationDialog.newInstance(_context.getString(selectedTopic.getDescriptionResource()), _context.getString(R.string.dismiss));
                dialog.show(((Activity) _context).getFragmentManager(), "MessageFragment");
            });
        }
    }


    /**
     * This is just a very simple Dialog that shows some help on what this topic is all about.
     * It also shows an OK button to dismiss.
     */
    public static class KATopicInformationDialog extends DialogFragment {

        private static final String DISMISS_BUTTON_TEXT = "dismiss_button";
        private static final String MESSAGE = "message";

        public KATopicInformationDialog() {
        }

        public static KATopicInformationDialog newInstance(String message, String okButtonText) {
            KATopicInformationDialog fragment = new KATopicInformationDialog();
            Bundle args = new Bundle();
            args.putString(MESSAGE, message);
            args.putString(DISMISS_BUTTON_TEXT, okButtonText);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(getArguments().getString(MESSAGE))
                    .setPositiveButton(getArguments().getString(DISMISS_BUTTON_TEXT), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


    /**
     * Communication interface used to pass data to any invoker of this spinner.
     */
    public interface OnKATopicsSpinnerAdapterListener {

        /**
         * Passes the KnowledgeAreaTopic id that was just selected from the spinner
         * Triggered on the OnClick callback of the Spinner.
         *
         * @param knowledgeAreaTopicId
         */
        void getSelectedTopicId(int knowledgeAreaTopicId);

    }
}
