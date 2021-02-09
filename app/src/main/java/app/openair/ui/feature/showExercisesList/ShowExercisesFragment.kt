package app.openair.ui.feature.showExercisesList

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.openair.R
import app.openair.ui.feature.recordExercise.RecordService
import app.openair.ui.feature.recordExercise.ServiceCallback
import app.openair.ui.feature.showSingleExercise.ShowExerciseDetailsActivity

/**
 * The fragment for showing the list of exercises on the main page
 * A fragment was used because it's under a TabLayout
 */
class ShowExercisesFragment : Fragment() {

    companion object {
        fun newInstance() = ShowExercisesFragment()

        const val SERVICE_CALLBACK_IDENTIFIER = "ShowExercisesFragment"
    }

    private var recordService: RecordService.MyBinder? = null
    private lateinit var viewModel: ShowExercisesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: ShowExercisesRecyclerViewAdapter
    private lateinit var selectionTracker: SelectionTracker<Long>
    private lateinit var currentSelection: Selection<Long>
    private lateinit var myContext: Context
    private lateinit var processingBanner: ConstraintLayout
    private lateinit var exerciseInProgressBanner: ConstraintLayout
    private lateinit var exerciseInProgressTime: TextView
    private lateinit var exerciseInProgressDistance: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // save context for later
        myContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShowExercisesViewModel::class.java)
        recyclerViewAdapter = ShowExercisesRecyclerViewAdapter(myContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // enable options menu handling in fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_exercises, container, false)
    }

    // TODO this probably shouldn't be connecting directly to the service #2
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            recordService = service as RecordService.MyBinder
            recordService?.registerCallback(SERVICE_CALLBACK_IDENTIFIER, callback)

            if (recordService?.getIsInProgress() == true) {
                // an exercise is awaiting processing
                exerciseInProgressBanner.visibility = View.VISIBLE
            } else {
                exerciseInProgressBanner.visibility = View.GONE
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            recordService?.unregisterCallback(SERVICE_CALLBACK_IDENTIFIER)
            recordService = null
        }
    }

    private val callback = object : ServiceCallback {
        override fun updateCurrentExerciseData(
            speed: Float,
            distance: Float
        ) {
            activity?.runOnUiThread {
                exerciseInProgressDistance.text = viewModel.formatDistance(distance)
            }
        }

        override fun updateCurrentTime(time: Long) {
            activity?.runOnUiThread {
                exerciseInProgressTime.text = viewModel.formatTime(time)
            }
        }

        override fun onStartTracking() {
            exerciseInProgressBanner.visibility = View.VISIBLE
        }

        override fun onStopTracking() {
            exerciseInProgressBanner.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        processingBanner = view.findViewById(R.id.processing_banner)
        exerciseInProgressBanner = view.findViewById(R.id.exercise_inProgressBanner)
        exerciseInProgressDistance = view.findViewById(R.id.text_exercise_distance)
        exerciseInProgressTime = view.findViewById(R.id.text_exercise_time)

        // connect to the recording service to see if any activities are in progress
        // this depends on exerciseInProgressBanner being initialised to prevent errors
        myContext.bindService(
            Intent(myContext, RecordService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

        // set up the recycler view to show the exercises
        recyclerView = view.findViewById(R.id.exercise_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(myContext)
        recyclerView.adapter = recyclerViewAdapter
        // set up multiple item select using long press
        trackSelectedItems()

        // get elements to display when no exercises are present
        val noContentTitle: TextView = view.findViewById(R.id.text_no_content_title)
        val noContentMessage: TextView = view.findViewById(R.id.text_no_content_message)

        // observe the LiveData and pass it to the adapter when it updates
        viewModel.exercises.observe(viewLifecycleOwner) {
            recyclerViewAdapter.data = it

            // show a message when there are no recorded exercises
            if (it.isNotEmpty()) {
                noContentTitle.visibility = View.INVISIBLE
                noContentMessage.visibility = View.INVISIBLE
            } else {
                noContentTitle.visibility = View.VISIBLE
                noContentMessage.visibility = View.VISIBLE
            }
        }

        // observe the status of the WorkManager queue for the Exercise processing jobs
        // this is used to show a banner when an activity is still being processed so may be missing details
        // it turns out the exercises are processed almost immediately, but I figured I'd leave this here anyway
        viewModel.processingExercise.observe(viewLifecycleOwner) {
            if (it) {
                // an exercise is awaiting processing
                processingBanner.visibility = View.VISIBLE
            } else {
                processingBanner.visibility = View.GONE
            }
        }
    }

    private fun openExercise(exerciseId: Long) {
        val intent = Intent(myContext, ShowExerciseDetailsActivity::class.java)
        val bundle = Bundle()

        bundle.putLong(ShowExerciseDetailsActivity.EXERCISE_ID_EXTRA, exerciseId)
        intent.putExtras(bundle)

        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        // hide the menu item that are only for when exercises are selected
        menu.findItem(R.id.action_clear)?.isVisible = false
        menu.findItem(R.id.action_delete)?.isVisible = false

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (!this::currentSelection.isInitialized) {
            // don't try and read an uninitialised variable
            return
        }
        if (currentSelection.isEmpty) {
            // hide selection specific options
            menu.findItem(R.id.action_clear)?.isVisible = false
            menu.findItem(R.id.action_delete)?.isVisible = false
        } else {
            // show selection specific options
            menu.findItem(R.id.action_clear)?.isVisible = true
            menu.findItem(R.id.action_delete)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_clear -> {
            selectionTracker.clearSelection()
            true
        }
        R.id.action_delete -> {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(
                getString(
                    R.string.showExerciseList_multiSelect_deleteDialog_message,
                    selectionTracker.selection.size()
                )
            )
                .setPositiveButton(
                    R.string.action_delete
                ) { _, _ ->
                    // once the user confirms then get the ids of the exercises to delete and delete them
                    val exerciseIdsToDelete: MutableList<Long> = mutableListOf()
                    selectionTracker.selection.forEach {
                        exerciseIdsToDelete.add(it)
                    }
                    viewModel.deleteExercises(exerciseIdsToDelete)
                    // then clear the selection tracker
                    selectionTracker.clearSelection()
                    // and tell recyclerview to update
                    recyclerViewAdapter.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.action_cancel, null)
            // Show the dialog
            builder.create().show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    override fun onPause() {
        super.onPause()

        // reset the actionbar title when you change tabs
        updateActionBar(true)
    }

    override fun onResume() {
        super.onResume()
        // restore the actionbar title when you change tabs
        updateActionBar()
    }

    private fun updateActionBar(clear: Boolean = false) {

        if (!this::currentSelection.isInitialized) {
            return
        }

        val actionbar = (activity as AppCompatActivity).supportActionBar ?: return

        if (clear || currentSelection.isEmpty) {
            actionbar.title = activity?.getString(R.string.app_name)
            actionbar.setBackgroundDrawable(
                activity?.getColor(R.color.primaryColor)?.let { ColorDrawable(it) }
            )
        } else {
            actionbar.title =
                getString(R.string.showExerciseList_multiSelect_selectedCount, currentSelection.size())
            actionbar.setBackgroundDrawable(
                activity?.getColor(R.color.primaryDarkColor)?.let { ColorDrawable(it) }
            )
        }

        // force a call to onPrepareOptionsMenu so we can change the menu items available
        activity?.invalidateOptionsMenu()
    }

    private fun trackSelectedItems() {

        selectionTracker = SelectionTracker.Builder(
            "selection-1",
            recyclerView,
            ItemIdKeyProvider(recyclerView),
            ItemLookup(recyclerView),
            StorageStrategy.createLongStorage()
        )
            // allow a normal tap (rather than long press) to open the exercise
            .withOnItemActivatedListener { item, _ ->
                item.selectionKey?.let {
                    openExercise(it)
                    true
                } ?: false
            }

            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        recyclerViewAdapter.setTracker(selectionTracker)

        selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                currentSelection = selectionTracker.selection
                updateActionBar()
            }
        })
    }

    inner class ItemIdKeyProvider(private val recyclerView: RecyclerView) :
        ItemKeyProvider<Long>(SCOPE_MAPPED) {

        override fun getKey(position: Int): Long {
            return recyclerView.adapter?.getItemId(position)
                ?: throw IllegalStateException("RecyclerView adapter is not set!")
        }

        override fun getPosition(key: Long): Int {
            val viewHolder = recyclerView.findViewHolderForItemId(key)
            return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
        }
    }

    class ItemLookup(private val rv: RecyclerView) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent)
                : ItemDetails<Long>? {

            val view = rv.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (rv.getChildViewHolder(view) as ShowExercisesRecyclerViewAdapter.ViewHolder)
                    .getItemDetails()
            }
            return null

        }
    }
}