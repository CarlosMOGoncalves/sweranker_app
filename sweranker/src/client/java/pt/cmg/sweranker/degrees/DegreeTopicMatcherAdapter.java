package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

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

    private List<KnowledgeArea> _knowledgeAreas;

    private Map<Integer, KnowledgeAreaTopic> _kaTopicsById;

    private DegreeClass _degreeClass;
    private Activity _context;
    private String[] _degreeTopicIds;
    private int[] _topicNameResources;
    private int _viewCount;


    /**
     * This object contains all the matches that exist in the system for the given Degree Class.
     * This is only empty in the admin flavour where it will be filled on this fragment, instead of just shown.
     */
    private DegreeClassMatch _currentMatches;


    public DegreeTopicMatcherAdapter(Activity context, DegreeClass degreeClass, List<KnowledgeArea> knowledgeAreas) {
        _context = context;
        _degreeClass = degreeClass;
        buildTopicArrays(degreeClass);
        _viewCount = degreeClass.getTopicCount();
        _knowledgeAreas = knowledgeAreas;
        _currentMatches = new DegreeClassMatch(_degreeClass);
        _kaTopicsById = getKATopicsById();

    }


    /**
     * Constructor used whenever there is already a match for this Degree Class.
     * It is useful because now we are updating and not creating a match.
     */
    public DegreeTopicMatcherAdapter(Activity context, DegreeClass degreeClass, DegreeClassMatch previousMatch, List<KnowledgeArea> knowledgeAreas) {
        _context = context;
        _degreeClass = degreeClass;
        buildTopicArrays(degreeClass);
        _viewCount = degreeClass.getTopicCount();
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


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_matcher_card, parent, false);
        return new DegreeClassTopicMatcherViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        DegreeClassTopicMatcherViewHolder currentHolder = ((DegreeClassTopicMatcherViewHolder) holder);

        currentHolder._degreeTopicName.setText(_context.getString(_topicNameResources[position]));
        currentHolder._topicMatchList.removeAllViews();

        createTopicMatches(currentHolder, position);

    }

    /**
     * Gets the holder and with side-effects appends all the topics that were matched to each program item
     * in the form of TextViews to the Layout.
     * This are a lot of words to say it constructs a sort of listview with the topics matched but colours it
     * and applies fancy styling.
     */
    private void createTopicMatches(DegreeClassTopicMatcherViewHolder holder, int adapterPosition) {

        String currentDegreeTopicId = _degreeTopicIds[adapterPosition];

        if (_currentMatches.hasTopicsSelected(currentDegreeTopicId)) {

            // some shortcuts so that I don't have to access them every time.
            float textSize = _context.getResources().getDimensionPixelSize(R.dimen.degree_matcher_topic_text_size);
            int padding = _context.getResources().getDimensionPixelSize(R.dimen.degree_matcher_topic_text_padding);

            // grab the matches for the current program item
            List<Integer> selectedKATopics = _currentMatches.getMatches(currentDegreeTopicId);

            for (int currentIndex = 0; currentIndex < selectedKATopics.size(); currentIndex++) {
                Integer currentKATopicId = selectedKATopics.get(currentIndex);

                // Now I just construct a TextView and style it.
                TextView topicName = new TextView(_context);
                topicName.setGravity(Gravity.END);
                topicName.setTextSize(textSize);
                topicName.setPadding(0, padding, 0, padding);
                topicName.setText(_context.getString(_kaTopicsById.get(currentKATopicId).getNameResource()));
                topicName.setTextColor(ContextCompat.getColor(_context, _kaTopicsById.get(currentKATopicId).getColorResource()));
                holder._topicMatchList.addView(topicName);
            }

        } else {
            // This should NEVER happen
            TextView emptyTextView = new TextView(_context);
            emptyTextView.setText("(no match)");
            holder._topicMatchList.addView(emptyTextView);
        }
    }


    @Override
    public int getItemCount() {
        return _viewCount;
    }


    /**
     * ViewHolder pattern to hold one of the matching cards.
     */
    public class DegreeClassTopicMatcherViewHolder extends RecyclerView.ViewHolder {

        private TextView _degreeTopicName;
        private LinearLayout _topicMatchList;


        public DegreeClassTopicMatcherViewHolder(View view) {
            super(view);
            _degreeTopicName = view.findViewById(R.id.topic_name);
            _topicMatchList = view.findViewById(R.id.match_list);
        }
    }


}
