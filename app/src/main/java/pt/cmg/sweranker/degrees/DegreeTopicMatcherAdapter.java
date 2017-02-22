package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.ui.materialspinner.MaterialSpinner;

/**
 * Created by Carlos on 15/02/2017.
 */

public class DegreeTopicMatcherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MATCHER = 10;
    private static final int TYPE_CONFIRM_BUTTON = 20;
    private static final int EXTRA_VIEW_COUNT = 1; // The button view...

    private List<KnowledgeArea> _knowledgeAreas;
    private DegreeClass _degreeClass;
    private Activity _context;
    private String[] _topicIds;
    private int[] _topicNameResources;
    private int _viewCount;

    private OnDegreeTopicMatcherListener _listener;


    public DegreeTopicMatcherAdapter(Activity context, DegreeClass degreeClass, List<KnowledgeArea> knowledgeAreas, OnDegreeTopicMatcherListener listener) {
        _context = context;
        _degreeClass = degreeClass;
        _listener = listener;
        buildTopicArrays(degreeClass);
        _viewCount = degreeClass.getTopicCount() + EXTRA_VIEW_COUNT;
        _knowledgeAreas = knowledgeAreas;

    }


    /**
     * Builds both arrays using the entry set of the degree program.
     *
     * @param degreeClass
     */
    private void buildTopicArrays(DegreeClass degreeClass) {
        _topicNameResources = new int[degreeClass.getTopicCount()];
        _topicIds = new String[degreeClass.getTopicCount()];

        int i = 0;
        for (Map.Entry<String, Integer> entry : degreeClass.getProgram().entrySet()) {
            _topicIds[i] = entry.getKey();
            _topicNameResources[i] = entry.getValue();
            i++;
        }
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
            ((DegreeClassTopicMatcherViewHolder) holder)._degreeTopicName.setText(_context.getString(_topicNameResources[position]));

        } else {

        }
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


            MaterialSpinner originalSpinner = new MaterialSpinner(_context);//(MaterialSpinner) view.findViewById(R.id.matcher_selector);
            originalSpinner.setAdapter(new KAMaterialSpinnerAdapter(_context, _knowledgeAreas, new KAMaterialSpinnerAdapter.OnKATopicsSpinnerAdapterListener() {
            }));
            _spinners.add(originalSpinner);

            _selectorContainer.addView(originalSpinner);

            _addMatcherButton = (ImageView) view.findViewById(R.id.uno_mas);
            _addMatcherButton.setColorFilter(ContextCompat.getColor(_context, R.color.materialAffirmative));
            _addMatcherButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    MaterialSpinner anotherSpinner = new MaterialSpinner(_context);//(MaterialSpinner) view.findViewById(R.id.matcher_selector);
                    anotherSpinner.setAdapter(new KAMaterialSpinnerAdapter(_context, _knowledgeAreas, new KAMaterialSpinnerAdapter.OnKATopicsSpinnerAdapterListener() {
                    }));
                    _spinners.add(anotherSpinner);
                    _selectorContainer.addView(anotherSpinner);

                    _removeMatherButton.setEnabled(true);
                    _removeMatherButton.setAlpha(1f);
                }
            });

            _removeMatherButton = (ImageView) view.findViewById(R.id.uno_menos);
            _removeMatherButton.setEnabled(false);
            _removeMatherButton.setAlpha(.3f);
            _removeMatherButton.setColorFilter(ContextCompat.getColor(_context, R.color.materialNegative));
            _removeMatherButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    _selectorContainer.removeViewAt(_spinners.size() - 1);
                    _spinners.remove(_spinners.size() - 1);

                    if (_spinners.size() == 1) {
                        _removeMatherButton.setEnabled(false);
                        _removeMatherButton.setAlpha(.3f);
                    }
                }
            });

        }
    }


    public class DegreeClassMatcherButton extends RecyclerView.ViewHolder {

        public DegreeClassMatcherButton(View view) {
            super(view);
        }
    }


    public interface OnDegreeTopicMatcherListener {

    }


}
