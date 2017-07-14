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

import java.util.HashMap;
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


    private DegreeClassMatch _currentMatches;


    private OnDegreeTopicMatcherListener _listener;


    public DegreeTopicMatcherAdapter(Activity context, DegreeClass degreeClass, List<KnowledgeArea> knowledgeAreas, OnDegreeTopicMatcherListener listener) {
        _context = context;
        _degreeClass = degreeClass;
        _listener = listener;
        buildTopicArrays(degreeClass);
        _viewCount = degreeClass.getTopicCount() + EXTRA_VIEW_COUNT;
        _knowledgeAreas = knowledgeAreas;
        _currentMatches = new DegreeClassMatch(_degreeClass);
        _kaTopicsById = getKATopicsById();

    }


    /**
     * Constructor used whenever there is already a match for this Degree Class.
     * It is useful because now we are updating and not creating a match.
     *
     * @param context
     * @param degreeClass
     * @param previousMatch
     * @param knowledgeAreas
     * @param listener
     */
    public DegreeTopicMatcherAdapter(Activity context, DegreeClass degreeClass, DegreeClassMatch previousMatch, List<KnowledgeArea> knowledgeAreas, OnDegreeTopicMatcherListener listener) {
        _context = context;
        _degreeClass = degreeClass;
        _listener = listener;
        buildTopicArrays(degreeClass);
        _viewCount = degreeClass.getTopicCount() + EXTRA_VIEW_COUNT;
        _knowledgeAreas = knowledgeAreas;
        _currentMatches = previousMatch;
        _kaTopicsById = getKATopicsById();

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

        } else {

        }
    }


    private void initialiseSpinners(DegreeClassTopicMatcherViewHolder holder, int adapterPosition) {

        String currentDegreeTopicId = _degreeTopicIds[adapterPosition];

        // Se tem pelo menos um seleccionado == 1 spinner pelo menos
        if (_currentMatches.hasTopicsSelected(currentDegreeTopicId)) {

            List<Integer> selectedKATopics = _currentMatches.getMatches(currentDegreeTopicId);
            for (int currentIndex = 0; currentIndex < selectedKATopics.size(); currentIndex++) {
                Integer currentKATopicId = selectedKATopics.get(currentIndex);

                MaterialSpinner selectedSpinner = initialiseSelectedSpinner(holder, currentDegreeTopicId, _kaTopicsById.get(currentKATopicId), currentIndex);
                holder._selectorContainer.addView(selectedSpinner);
            }

        } else {
            MaterialSpinner emptySpinner = initialiseEmptySpinner(holder, currentDegreeTopicId, 0);
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
    private MaterialSpinner initialiseSelectedSpinner(DegreeClassTopicMatcherViewHolder holder, String degreeTopicId, KnowledgeAreaTopic selectedKATopic, int selectedTopicIndex) {

        MaterialSpinner kaTopicSpinner = new MaterialSpinner(_context, true);
        kaTopicSpinner.setAdapter(new KAMaterialSpinnerAdapter(_context, _knowledgeAreas, new KAMaterialSpinnerAdapter.OnKATopicsSpinnerAdapterListener() {
            @Override
            public void getSelectedTopicId(int knowledgeTopicId) {
                // I add the selected item to the matching list, obtained by getting it from the _topicsIds array
                addSelectedTopic(degreeTopicId, knowledgeTopicId, selectedTopicIndex);
                holder._addMatcherButton.setAlpha(1f);
                holder._addMatcherButton.setEnabled(true);
            }
        }));
        kaTopicSpinner.setTextColor(ContextCompat.getColor(_context, selectedKATopic.getColorResource()));
        kaTopicSpinner.setText(_context.getString(selectedKATopic.getNameResource()));

        // HERE: with this deactivated this spinner is in effect a read-only. It must just be active in development
        // I should probably just toy around with flavours to create a dev specific version. Noted.
        kaTopicSpinner.setEnabled(false);

        return kaTopicSpinner;
    }


    /**
     * Initialises and returns an empty Material Spinner, i.e. one that has no topic yet selected and thus is in the
     * default state for this kind of Spinner.
     * <p>
     * Additionally this kind has a listener that adds a new selected topic to the internal data structure that handles it
     * so that I can control the KA Topics that were already selected for this Program Topic.
     *
     * @return
     */
    private MaterialSpinner initialiseEmptySpinner(DegreeClassTopicMatcherViewHolder holder, String degreeTopicId, int positionIndex) {
        MaterialSpinner kaTopicSpinner = new MaterialSpinner(_context, true);
        kaTopicSpinner.setAdapter(new KAMaterialSpinnerAdapter(_context, _knowledgeAreas, new KAMaterialSpinnerAdapter.OnKATopicsSpinnerAdapterListener() {
            @Override
            public void getSelectedTopicId(int knowledgeTopicId) {
                // Adds the selected item to the matching list
                addSelectedTopic(degreeTopicId, knowledgeTopicId, positionIndex);
                holder._addMatcherButton.setAlpha(1f);
                holder._addMatcherButton.setEnabled(true);
            }
        }));

        // HERE: with this deactivated this spinner is in effect a read-only. It must just be active in development
        // I should probably just toy around with flavours to create a dev specific version. Noted.
        kaTopicSpinner.setEnabled(false);

        return kaTopicSpinner;
    }


    /**
     * Adds or replaces a KA Topic in the matches of a Degree Topic.
     * Additionally this will be the trigger to whether the submit button can be made active or not.
     *
     * @param degreeTopicId
     * @param kaTopicId
     * @param positionIndex
     */
    private void addSelectedTopic(String degreeTopicId, int kaTopicId, int positionIndex) {

        int selectedKaTopicsCount = _currentMatches.getKATopicsCount(degreeTopicId);

        // If it is the first topic to be added OR the latest (i.e. adding a new one) we add it to the collection
        if (selectedKaTopicsCount == 0 || selectedKaTopicsCount == positionIndex) {
            _currentMatches.addKATopicToDegreeTopic(degreeTopicId, kaTopicId);
        } else { // Or else it is an update
            _currentMatches.replaceSelectedKATopic(degreeTopicId, kaTopicId, positionIndex);
        }

        // After adding, we check whether ALL of them have at least one selected topic and if it has then we
        // enable the submit button, BUT ONLY if it is already visible, as per the functionality of the Recycler View adapter.
        if (_submitButton != null && _currentMatches.isCompleteMatch()) {
            _submitButton.setEnabled(true);
            _submitButton.setAlpha(1f);
        }
    }


    /**
     * Initialises the Add Topic button.
     * This part is heavy in logic. Simply put, it needs to know when to enable itself and the Remove Topic button.
     * Also it gets its onClickListener to add a new Spinner whenever it can (to add another topic).
     *
     * @param holder
     * @param adapterPosition
     */
    private void initialiseAddMatchButton(DegreeClassTopicMatcherViewHolder holder, int adapterPosition) {

        String currentDegreeTopicId = _degreeTopicIds[adapterPosition];

        int numberOfSelectedKaTopics = _currentMatches.getKATopicsCount(currentDegreeTopicId);

        // If there is still no ka topic selected then it is the first one and so there is no need to add more yet.
        if (numberOfSelectedKaTopics == 0) {
            holder._addMatcherButton.setAlpha(0.3f);
            holder._addMatcherButton.setEnabled(false);
        }

        holder._addMatcherButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Basically: how many spinners does this topic already has?
                int nextSpinnerPositionIndex = _currentMatches.getKATopicsCount(currentDegreeTopicId);
                MaterialSpinner anotherSpinner = initialiseEmptySpinner(holder, currentDegreeTopicId, nextSpinnerPositionIndex);

                holder._selectorContainer.addView(anotherSpinner);

                holder._removeMatherButton.setEnabled(true);
                holder._removeMatherButton.setAlpha(1f);

                holder._addMatcherButton.setEnabled(false);
                holder._addMatcherButton.setAlpha(0.3f);
            }
        });
    }

    /**
     * Initialises the Remove Topic button.
     * This part is heavy in logic. Simply put, it needs to know when to enable itself and the Add Topic button.
     * <p>
     * This button is limited to delete the last match that was input in the spinner.
     * This is a feature, because I didn't want to complicate the Spinner by allowing any topic to be deleted.
     *
     * @param holder
     * @param adapterPosition
     */
    private void initialiseRemoveMatchButton(DegreeClassTopicMatcherViewHolder holder, int adapterPosition) {

        String currentDegreeTopicId = _degreeTopicIds[adapterPosition];

        if (_currentMatches.getKATopicsCount(currentDegreeTopicId) <= 1) {
            holder._removeMatherButton.setEnabled(false);
            holder._removeMatherButton.setAlpha(.3f);
        } else {
            holder._removeMatherButton.setEnabled(true);
            holder._removeMatherButton.setAlpha(1f);
        }


        holder._removeMatherButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int numberOfCurrentSpinners = holder._selectorContainer.getChildCount();
                int numberOfSelectedKaTopics = _currentMatches.getKATopicsCount(currentDegreeTopicId);

                // If I have more spinners than actually selected topics then it means it is a still empty spinner (with nothing selected)
                // In that case I just remove it from the view hierarchy
                if (numberOfCurrentSpinners > numberOfSelectedKaTopics) {
                    holder._selectorContainer.removeViewAt(numberOfCurrentSpinners - 1);
                    numberOfCurrentSpinners--;
                }
                // Else it was an already selected spinner which means that more than the view hierarchy I also have
                // to erase it from the selected ka topics data structure.
                else {
                    holder._selectorContainer.removeViewAt(numberOfCurrentSpinners - 1);
                    numberOfCurrentSpinners--;
                    _currentMatches.removeLastKATopicAdded(currentDegreeTopicId);
                }

                // Then, re-enable the add button because we can add a new topic now
                holder._addMatcherButton.setEnabled(true);
                holder._addMatcherButton.setAlpha(1f);

                if (numberOfCurrentSpinners == 1) {
                    holder._removeMatherButton.setEnabled(false);
                    holder._removeMatherButton.setAlpha(.3f);
                }


            }
        });
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
     * ViewHolder pattern to hold one of the matching cards.
     */
    public class DegreeClassTopicMatcherViewHolder extends RecyclerView.ViewHolder {

        private TextView _degreeTopicName;
        private LinearLayout _selectorContainer;
        private ImageView _addMatcherButton;
        private ImageView _removeMatherButton;


        public DegreeClassTopicMatcherViewHolder(View view) {
            super(view);
            _degreeTopicName = (TextView) view.findViewById(R.id.topic_name);
            _selectorContainer = (LinearLayout) view.findViewById(R.id.selector_container);

            _addMatcherButton = (ImageView) view.findViewById(R.id.uno_mas);
            _addMatcherButton.setColorFilter(ContextCompat.getColor(_context, R.color.materialAffirmative));

            _removeMatherButton = (ImageView) view.findViewById(R.id.uno_menos);
            _removeMatherButton.setColorFilter(ContextCompat.getColor(_context, R.color.materialNegative));

            // HERE AGAIN: I am removing these buttons so that nobody can touch them, only in dev mode
            _addMatcherButton.setVisibility(View.GONE);
            _removeMatherButton.setVisibility(View.GONE);

        }

    }


    public class DegreeClassMatcherButton extends RecyclerView.ViewHolder {


        public DegreeClassMatcherButton(View view) {
            super(view);
            _submitButton = (Button) view.findViewById(R.id.match_button);

            // Enable it or disable the button depending on whether it has matches for every class or not.
            if (_currentMatches.isCompleteMatch()) {
                _submitButton.setEnabled(true);
                _submitButton.setAlpha(1f);
            } else {
                _submitButton.setEnabled(false);
                _submitButton.setAlpha(.3f);
            }

            // HERE AGAIN: I am removing this button so that nobody can touch it, only in dev mode
            _submitButton.setVisibility(View.GONE);

            _submitButton.setOnClickListener(v -> _listener.onMatchSubmitted(_currentMatches));
        }
    }


    /**
     * Communication interface.
     * Here are functions to be called whenever something important happens
     * on this adapter.
     */
    public interface OnDegreeTopicMatcherListener {


        /**
         * Triggered when a complete match of KA Topics to a Degree Class has been submitted
         */
        void onMatchSubmitted(DegreeClassMatch selectedMatch);

    }


}
