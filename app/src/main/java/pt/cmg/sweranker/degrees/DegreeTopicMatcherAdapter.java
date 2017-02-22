package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;
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
        private MaterialSpinner _kaTopicSpinner;

        public DegreeClassTopicMatcherViewHolder(View view) {
            super(view);
            _degreeTopicName = (TextView) view.findViewById(R.id.topic_name);
            _kaTopicSpinner = (MaterialSpinner) view.findViewById(R.id.matcher_selector);

            KAMaterialSpinnerAdapter adapter = new KAMaterialSpinnerAdapter(_context, _knowledgeAreas, new KAMaterialSpinnerAdapter.OnKATopicsSpinnerAdapterListener() {

                @Override
                public void onInfoButtonClicked(int kaTopicDescription) {
                    KATopicInformationDialog dialog = KATopicInformationDialog.newInstance(_context.getString(kaTopicDescription));
                    dialog.show(_context.getFragmentManager(), "allah");
                }

                @Override
                public void onTopicViewClicked(KnowledgeAreaTopic kaTopic) {
                    _kaTopicSpinner.setSelectedItemText(_context.getString(kaTopic.getNameResource()));
                    _kaTopicSpinner.setSelectedObject(kaTopic);
                    _kaTopicSpinner.collapse();
                }
            });
            _kaTopicSpinner.setAdapter(adapter);
        }
    }

    public static class KATopicInformationDialog extends DialogFragment {

        private static final String MESSAGE = "message";

        private String _message;

        public KATopicInformationDialog() {
        }

        public static KATopicInformationDialog newInstance(String message) {
            KATopicInformationDialog fragment = new KATopicInformationDialog();
            Bundle args = new Bundle();
            args.putString(MESSAGE, message);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getArguments().getString(MESSAGE))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
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
