package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;
import pt.cmg.sweranker.ui.materialspinner.MaterialSpinner;

/**
 * This Adapter is used to convert a {@link DegreeClass} in a list where each row
 * is one of the topics of its Degree Program with a widget to chose the {@link KnowledgeAreaTopic}
 * that it matches with. One or more topics that is, as each item of the program can match
 * with more than one knowledge area topic.
 * <p>
 * In addition, this adapter will also keep and return the matches that were made in its views.
 * As awkward as it is, this class will also be responsible for controlling the inputs made in
 * its views (i.e. the matches that were made).
 * <p>
 * Created by Carlos on 15/02/2017.
 */

public class DegreeTopicMatcherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MATCHER = 10;
    private static final int TYPE_CONFIRM_BUTTON = 20;
    private static final int EXTRA_VIEW_COUNT = 1; // The button view...

    private List<KnowledgeArea> _knowledgeAreas;


    private Map<Integer, KnowledgeAreaTopic> _kaTopicsById;

    private DegreeClass _degreeClass;
    private Activity _context;
    private String[] _degreeTopicIds;
    private int[] _topicNameResources;
    private int _viewCount;


    private Button _submitButton;

    /**
     * Keys -> Degree Program Item , Values -> the ids of each KnowledgeAreaTopic matched.
     * I used the LinkedList because it is easier to remove the last element, which will be done
     * a bit.
     */
    private Map<String, LinkedList<Integer>> _selectedKATopicsByProgramItem;

    /**
     * True if it already has a match for every item of this degree class program.
     */
    private boolean _hasCompleteMatch;

    private OnDegreeTopicMatcherListener _listener;


    public DegreeTopicMatcherAdapter(Activity context, DegreeClass degreeClass, List<KnowledgeArea> knowledgeAreas, OnDegreeTopicMatcherListener listener) {
        _context = context;
        _degreeClass = degreeClass;
        _listener = listener;
        buildTopicArrays(degreeClass);
        _viewCount = degreeClass.getTopicCount() + EXTRA_VIEW_COUNT;
        _knowledgeAreas = knowledgeAreas;
        _selectedKATopicsByProgramItem = initialiseMatchTrackers();

        _kaTopicsById = getKATopicsById();

        _hasCompleteMatch = false;
    }


    /**
     * Builds both arrays using the entry set of the degree program.
     *
     * @param degreeClass
     */
    private void buildTopicArrays(DegreeClass degreeClass) {
        _topicNameResources = new int[degreeClass.getTopicCount()];
        _degreeTopicIds = new String[degreeClass.getTopicCount()];

        int i = 0;
        for (Map.Entry<String, Integer> entry : degreeClass.getProgram().entrySet()) {
            _degreeTopicIds[i] = entry.getKey();
            _topicNameResources[i] = entry.getValue();
            i++;
        }
    }


    private Map<String, LinkedList<Integer>> initialiseMatchTrackers() {
        Map<String, LinkedList<Integer>> matches = new HashMap<>(_degreeTopicIds.length);

//        Arrays.stream(_degreeTopicIds).forEach(topicId -> matches.put(topicId, new LinkedList<>()));

        for (int i = 0; i < _degreeTopicIds.length; i++) {
            matches.put(_degreeTopicIds[i], new LinkedList<>());
        }

        return matches;
    }


    /**
     * @return
     */
    private Map<Integer, KnowledgeAreaTopic> getKATopicsById() {
        Map<Integer, KnowledgeAreaTopic> kaTopics = new HashMap<>();

        for (KnowledgeArea ka : _knowledgeAreas) {
            for (KnowledgeAreaTopic kaTopic : ka.getTopics()) {
                kaTopics.put(kaTopic.getId(), kaTopic);
            }
        }

        return kaTopics;
    }

    /**
     * Returns true if there is at least one KA Topic already selected for this Degree topic
     *
     * @param degreeTopic
     * @return
     */
    private boolean hasTopicsSelected(String degreeTopic) {
        return !_selectedKATopicsByProgramItem.get(degreeTopic).isEmpty();
    }

    /**
     * Basically calculates the number of Knowledge Areas plus the total combined of each Knowledge Area's topics.
     *
     * @return
     */
    private int calculateKAItemCount() {

        int total = 0;
        for (KnowledgeArea ka : _knowledgeAreas) {
            total += ka.getTopicsCount();
        }
        total += _knowledgeAreas.size();

        return total;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        if (viewType == TYPE_MATCHER) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_matcher_card, parent, false);
            return new DegreeClassTopicMatcherViewHolder(itemView);
        }

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_matcher_button, parent, false);
        return new DegreeClassMatcherButton(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DegreeClassTopicMatcherViewHolder) {

            DegreeClassTopicMatcherViewHolder currentHolder = ((DegreeClassTopicMatcherViewHolder) holder);

            currentHolder._degreeTopicName.setText(_context.getString(_topicNameResources[position]));
            currentHolder._selectorContainer.removeAllViews();

            initialiseSpinners(currentHolder, position);

            initialiseAddMatchButton(currentHolder, position);

            initialiseRemoveMatchButton(currentHolder, position);

        } else

        {

        }
    }


    private void initialiseSpinners(DegreeClassTopicMatcherViewHolder holder, int adapterPosition) {

        if (hasTopicsSelected(_degreeTopicIds[adapterPosition])) {

            List<Integer> selectedKATopics = _selectedKATopicsByProgramItem.get(_degreeTopicIds[adapterPosition]);
            for (int currentIndex = 0; currentIndex < selectedKATopics.size(); currentIndex++) {
                Integer currentTopicId = selectedKATopics.get(currentIndex);

                MaterialSpinner selectedSpinner = initialiseSelectedSpinner(holder, adapterPosition, _kaTopicsById.get(currentTopicId), currentIndex);
                holder._spinners.add(selectedSpinner);
                holder._selectorContainer.addView(selectedSpinner);
            }

        } else {
            MaterialSpinner emptySpinner = initialiseEmptySpinner(holder, adapterPosition);
            holder._spinners.add(emptySpinner);
            holder._selectorContainer.addView(emptySpinner);
        }
    }


    /**
     * Initialises and returns a selected Material Spinner, i.e. one that has already a previously selected topic appended.
     * This is specially tweaked so that it is correctly painted.
     * TODO: this painting stuff must be made by either the adapter or the spinner in reaction to the selected element and not here.
     * <p>
     * Additionally this adds a listener any new selected KA Topic to replace the previous
     *
     * @param selectedKATopic
     * @return
     */
    private MaterialSpinner initialiseSelectedSpinner(DegreeClassTopicMatcherViewHolder holder, int adapterPosition, KnowledgeAreaTopic selectedKATopic, int selectedTopicIndex) {

        MaterialSpinner kaTopicSpinner = new MaterialSpinner(_context);
        kaTopicSpinner.setAdapter(new KAMaterialSpinnerAdapter(_context, _knowledgeAreas, new KAMaterialSpinnerAdapter.OnKATopicsSpinnerAdapterListener() {
            @Override
            public void getSelectedTopicId(int knowledgeTopicId) {
                // I add the selected item to the matching list, obtained by getting it from the _topicsIds array
                replaceSelectedTopic(_degreeTopicIds[adapterPosition], knowledgeTopicId, selectedTopicIndex);
                holder._addMatcherButton.setAlpha(1f);
                holder._addMatcherButton.setEnabled(true);
            }
        }));
        kaTopicSpinner.setTextColor(ContextCompat.getColor(_context, selectedKATopic.getColorResource()));
        kaTopicSpinner.setText(_context.getString(selectedKATopic.getNameResource()));

        return kaTopicSpinner;
    }

    private void replaceSelectedTopic(String degreeTopicId, int updatedKaTopicId, int selectedTopicIdIndex) {
        _selectedKATopicsByProgramItem.get(degreeTopicId).remove(selectedTopicIdIndex);
        _selectedKATopicsByProgramItem.get(degreeTopicId).add(selectedTopicIdIndex, updatedKaTopicId);
    }

    /**
     * Initialises and returns an empty Material Spinner, i.e. one that has no topic yet selected and thus is in the
     * default state for this kind of Spinner.
     * <p>
     * Additionally this kind has a listener that adds a new selected topic to the internal data structure that handles it
     * so that I can control the KA Topics that were already selected for this Program Topic.
     *
     * @param adapterPosition
     * @return
     */
    private MaterialSpinner initialiseEmptySpinner(DegreeClassTopicMatcherViewHolder holder, int adapterPosition) {
        MaterialSpinner kaTopicSpinner = new MaterialSpinner(_context);
        kaTopicSpinner.setAdapter(new KAMaterialSpinnerAdapter(_context, _knowledgeAreas, new KAMaterialSpinnerAdapter.OnKATopicsSpinnerAdapterListener() {
            @Override
            public void getSelectedTopicId(int knowledgeTopicId) {
                // I add the selected item to the matching list, obtained by getting it from the _topicsIds array
                addSelectedTopic(_degreeTopicIds[adapterPosition], knowledgeTopicId);
                holder._addMatcherButton.setAlpha(1f);
                holder._addMatcherButton.setEnabled(true);
            }
        }));
        return kaTopicSpinner;
    }


    private void addSelectedTopic(String degreeTopicId, int kaTopicId) {
        _selectedKATopicsByProgramItem.get(degreeTopicId).add(kaTopicId);

        if (_selectedKATopicsByProgramItem.values().stream().noneMatch(list -> list.isEmpty())) {
            _hasCompleteMatch = true;

            _submitButton.setEnabled(true);
            _submitButton.setAlpha(1f);
        }

    }


    private void initialiseAddMatchButton(DegreeClassTopicMatcherViewHolder holder, int adapterPosition) {
        holder._addMatcherButton.setColorFilter(ContextCompat.getColor(_context, R.color.materialAffirmative));
        holder._addMatcherButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                MaterialSpinner anotherSpinner = initialiseEmptySpinner(holder, adapterPosition);
                holder._spinners.add(anotherSpinner);
                holder._selectorContainer.addView(anotherSpinner);

                holder._removeMatherButton.setEnabled(true);
                holder._removeMatherButton.setAlpha(1f);

                holder._addMatcherButton.setEnabled(false);
                holder._addMatcherButton.setAlpha(0.3f);
            }
        });
    }

    private void initialiseRemoveMatchButton(DegreeClassTopicMatcherViewHolder holder, int adapterPosition) {

        holder._removeMatherButton.setColorFilter(ContextCompat.getColor(_context, R.color.materialNegative));

        if (_selectedKATopicsByProgramItem.get(_degreeTopicIds[adapterPosition]).size() <= 1) {
            holder._removeMatherButton.setEnabled(false);
            holder._removeMatherButton.setAlpha(.3f);
        } else {
            holder._removeMatherButton.setEnabled(true);
            holder._removeMatherButton.setAlpha(1f);
        }


        holder._removeMatherButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Remove all that it has done, namely the view added to the layout, the spinner and any previously selected topic.
                holder._selectorContainer.removeViewAt(holder._spinners.size() - 1);
                holder._spinners.remove(holder._spinners.size() - 1);
                removeSelectedTopicId(_degreeTopicIds[adapterPosition]);


                holder._addMatcherButton.setEnabled(true);
                holder._addMatcherButton.setAlpha(1f);

                if (holder._spinners.size() == 1) {
                    holder._removeMatherButton.setEnabled(false);
                    holder._removeMatherButton.setAlpha(.3f);
                }


            }
        });
    }

    private void removeSelectedTopicId(String degreeTopicId) {
        _selectedKATopicsByProgramItem.get(degreeTopicId).removeLast();
    }

    @Override
    public int getItemCount() {
        return _viewCount;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == (getItemCount() - 1)) { // it is the last position
            return TYPE_CONFIRM_BUTTON;
        }
        return TYPE_MATCHER;
    }


    /**
     * ViewHolder pattern to hold one of the cards
     */
    public class DegreeClassTopicMatcherViewHolder extends RecyclerView.ViewHolder {

        private TextView _degreeTopicName;
        private LinearLayout _selectorContainer;
        private List<MaterialSpinner> _spinners;
        private ImageView _addMatcherButton;
        private ImageView _removeMatherButton;


        public DegreeClassTopicMatcherViewHolder(View view) {
            super(view);
            _degreeTopicName = (TextView) view.findViewById(R.id.topic_name);
            _selectorContainer = (LinearLayout) view.findViewById(R.id.selector_container);
            _spinners = new ArrayList<>();

            _addMatcherButton = (ImageView) view.findViewById(R.id.uno_mas);
            _removeMatherButton = (ImageView) view.findViewById(R.id.uno_menos);

        }

    }


    public class DegreeClassMatcherButton extends RecyclerView.ViewHolder {


        public DegreeClassMatcherButton(View view) {
            super(view);
            _submitButton = (Button) view.findViewById(R.id.match_button);
            _submitButton.setEnabled(false);
            _submitButton.setAlpha(.3f);

            _submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(_context, "Allahu akbar", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public interface OnDegreeTopicMatcherListener {

    }


}
