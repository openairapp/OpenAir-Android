package app.openair.ui.feature.showExercisesList

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import app.openair.R
import app.openair.ui.formatter.FormattedExercise

class ShowExercisesRecyclerViewAdapter(val context: Context) :
    RecyclerView.Adapter<ShowExercisesRecyclerViewAdapter.ViewHolder>() {

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    var data: List<FormattedExercise> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var tracker: SelectionTracker<Long>? = null

    init {
        // we can set this since getItemId() bases the id on the actual element data
        setHasStableIds(true)
    }

    fun setTracker(tracker: SelectionTracker<Long>?) {
        this.tracker = tracker
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewData = data[position]
        holder.bind(viewData)

        val container = holder.cardView
        if(tracker!!.isSelected(viewData.id)){
            container.background = ColorDrawable(context.getColor(R.color.secondaryLightColor))
        } else {
            // Reset color to white if not selected
            container.background = ColorDrawable(Color.WHITE)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return data[position].id
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var exerciseId: Long? = null
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val dateTextView: TextView = itemView.findViewById(R.id.text_exercise_date)
        private val nameTextView: TextView = itemView.findViewById(R.id.text_exercise_name)
        private val distanceTextView: TextView = itemView.findViewById(R.id.text_exercise_distance)
        private val durationTextView: TextView = itemView.findViewById(R.id.text_exercise_duration)
        private val elevationTextView: TextView = itemView.findViewById(R.id.text_exercise_elevationGain)

        fun bind(item: FormattedExercise) {
            exerciseId = item.id

            dateTextView.text = item.date
            nameTextView.text = item.name
            distanceTextView.text = item.distance
            durationTextView.text = item.duration
            elevationTextView.text = item.elevation
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object: ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = exerciseId
            }
    }
}