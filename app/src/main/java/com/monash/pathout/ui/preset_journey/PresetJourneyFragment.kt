package com.monash.pathout.ui.preset_journey

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.search.*
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.monash.pathout.R
import com.monash.pathout.databinding.PresetJourneyFragmentBinding
import com.monash.pathout.entity.Journey
import com.monash.pathout.ui.NavigationActivity
import com.monash.pathout.viewmodel.JourneyViewModel

class PresetJourneyFragment : Fragment() {
    private lateinit var searchEngine: SearchEngine
    private var searchRequestTask: SearchRequestTask? = null

    private var binding: PresetJourneyFragmentBinding? = null
    private var viewModel: JourneyViewModel? = null
    private var initRV = false
    private val presetJourneysArrayList: MutableList<Journey> = ArrayList()
    private var adapter: PresetJourneyAdapter? = null

    private var journeyNameET: EditText? = null
    private var startPointATV: AutoCompleteTextView? = null
    private var endPointATV: AutoCompleteTextView? = null
    private var selectedStart: SearchResult? = null
    private var selectedEnd: SearchResult? = null
    private var loadedStartPlaces: List<SearchSuggestion> = ArrayList()
    private var loadedEndPlaces: List<SearchSuggestion> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[JourneyViewModel::class.java]
        initRV = false

        searchEngine = MapboxSearchSdk.getSearchEngine()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the View for this fragment
        binding = PresetJourneyFragmentBinding.inflate(inflater, container, false)

        // Observe livedata of preset journeys
        viewModel?.allJourneys?.observe(requireActivity(), Observer { journeys ->
            presetJourneysArrayList.clear()
            presetJourneysArrayList.addAll(journeys)
            adapter?.notifyDataSetChanged()
        })

        setupRecyclerView()
        setAddJourneyClickListener()
        return binding!!.root
    }

    override fun onDestroy() {
        searchRequestTask?.cancel()
        super.onDestroy()
    }

    private val searchCallback = object : SearchSelectionCallback {
        var isEditingStartPoint = true

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            if (suggestions.isEmpty()) {
                Log.i(TAG, "No suggestions found")
            } else {
                val placesNames = suggestions.map { "${it.name}" }
                val arrayAdapter: ArrayAdapter<String>?

                Log.i(TAG, "$isEditingStartPoint\n $placesNames")

                arrayAdapter = object : ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_list_item_2,
                    android.R.id.text1,
                    placesNames
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val text1 = view.findViewById(android.R.id.text1) as TextView
                        val text2 = view.findViewById(android.R.id.text2) as TextView

                        val address = suggestions[position].address
                        var locality = address?.locality ?: ""
                        var postcode = address?.postcode ?: ""
                        var country = address?.country ?: ""

                        if (locality.isNotBlank()) {
                            locality += ", "
                        }

                        if (postcode.isNotBlank() and country.isNotBlank()) {
                            postcode += ", "
                        }

                        text1.text = placesNames[position]
                        text2.text =
                            "$locality${address?.region ?: ""} $postcode${address?.country ?: ""}"

                        return view
                    }
                }

                if (isEditingStartPoint) {
                    this@PresetJourneyFragment.loadedStartPlaces = suggestions
                    this@PresetJourneyFragment.startPointATV?.setAdapter(arrayAdapter)
                    this@PresetJourneyFragment.startPointATV?.showDropDown()
                } else {
                    this@PresetJourneyFragment.loadedEndPlaces = suggestions
                    this@PresetJourneyFragment.endPointATV?.setAdapter(arrayAdapter)
                    this@PresetJourneyFragment.endPointATV?.showDropDown()
                }

                arrayAdapter.notifyDataSetChanged()
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            Log.i(TAG, "Search result: $result")

            if (isEditingStartPoint) {
                selectedStart = result
            } else {
                selectedEnd = result
            }
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            Log.i(TAG, "Category search results: $results")
        }

        override fun onError(e: Exception) {
            Log.i(TAG, "Search error", e)
        }
    }

    private fun setupRecyclerView() {
        val stopRV = binding!!.idRVPresetJourneys
        val linearLayoutManager = LinearLayoutManager(
            requireActivity().applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        val presetJourneysAdapter =
            viewModel?.let {
                PresetJourneyAdapter(
                    requireActivity(),
                    this.presetJourneysArrayList,
                    it,
                    setEditJourneyClickListener(),
                    onPresetJourneyNavigate()
                )
            }

        stopRV.layoutManager = linearLayoutManager
        stopRV.adapter = presetJourneysAdapter
        stopRV.itemAnimator = DefaultItemAnimator()

        this.adapter = stopRV.adapter as PresetJourneyAdapter?

        initRV = true
    }

    private fun onPresetJourneyNavigate(): (Journey) -> Unit {
        return fun(journey: Journey) {
            val bundle = Bundle()
            bundle.putParcelable("JOURNEY", journey)
            bundle.putString("DESTINATION_NAME", journey.destinationName)

            Intent(requireContext(), NavigationActivity::class.java).run {
                putExtras(bundle)
                startActivity(this)
            }
        }
    }

    private fun setAddJourneyClickListener() {
        binding?.addPJ?.setOnClickListener {
            val view: View = layoutInflater.inflate(R.layout.add_preset_journey_dialog, null)

            journeyNameET = view.findViewById(R.id.journeyNameET)
            startPointATV = view.findViewById(R.id.journeyStartATV)
            endPointATV = view.findViewById(R.id.journeyEndATV)

            // Extension function for delayed Search box autocomplete
            fun AutoCompleteTextView.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
                this.addTextChangedListener(object : TextWatcher {
                    var timer: CountDownTimer? = null

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(editable: Editable?) {
                        if (!isPerformingCompletion) {
                            timer?.cancel()
                            timer = object : CountDownTimer(1000, 1500) {
                                override fun onTick(millisUntilFinished: Long) {}
                                override fun onFinish() {
                                    afterTextChanged.invoke(editable.toString())
                                }
                            }.start()
                        } else {
                            // Get the SearchResult of item that is selected from Mapbox
                            if (id == R.id.journeyStartATV) {
                                // Find the search suggestion based on name
                                val selectedStartSuggestion =
                                    loadedStartPlaces.firstOrNull { it.name == editable.toString() }

                                searchRequestTask =
                                    selectedStartSuggestion?.let { it1 ->
                                        searchEngine.select(
                                            it1,
                                            searchCallback
                                        )
                                    }!!
                            } else {
                                // Find the search suggestion based on name
                                val selectedEndSuggestion =
                                    loadedEndPlaces.firstOrNull { it.name == editable.toString() }

                                searchRequestTask =
                                    selectedEndSuggestion?.let { it1 ->
                                        searchEngine.select(
                                            it1,
                                            searchCallback
                                        )
                                    }!!
                            }
                        }
                    }
                })
            }

            startPointATV?.afterTextChangedDelayed {
                searchEngine.search(
                    it,
                    SearchOptions(limit = 5),
                    searchCallback.apply { isEditingStartPoint = true })
            }

            endPointATV?.afterTextChangedDelayed {
                searchEngine.search(
                    it,
                    SearchOptions(limit = 5),
                    searchCallback.apply { isEditingStartPoint = false })
            }

            val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add a new preset journey")
                .setNeutralButton("Cancel") { dialog, which ->
                    // Respond to neutral button press
                }
                .setPositiveButton("Create", null)
                .setView(view)

            val addJourneyDialog = dialogBuilder.create()
            addJourneyDialog.show()

            // Override the button behaviour to prevent it from dismissing the dialog before passing all validation
            addJourneyDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                var passedValidation = true
                startPointATV?.error = null
                endPointATV?.error = null

                // Respond to positive button press
                if (journeyNameET?.text?.isBlank() == true) {
                    journeyNameET?.error = "Please provide journey name"
                    passedValidation = false
                }

                if (selectedStart == null) {
                    startPointATV?.error = "Please provide a starting point for the journey"
                    passedValidation = false
                }

                if (selectedEnd == null) {
                    endPointATV?.error = "Please provide an end point for the journey"
                    passedValidation = false
                }

                if (passedValidation) {
                    val startCoordinate = selectedStart?.coordinate
                    val endCoordinate = selectedEnd?.coordinate

                    val startAddress = selectedStart?.address
                    val endAddress = selectedEnd?.address


                    val journey = Journey(
                        journeyNameET?.text.toString(),
                        selectedStart?.name,
                        getAddressStr(startAddress),
                        startCoordinate?.latitude(),
                        startCoordinate?.longitude(),
                        selectedEnd?.name,
                        getAddressStr(endAddress),
                        endCoordinate?.latitude(),
                        endCoordinate?.longitude()
                    )

                    viewModel?.insert(journey)
                    passedValidation = true
                }

                if (passedValidation) {
                    // Clear variables storing the values
                    selectedStart = null
                    selectedEnd = null

                    addJourneyDialog.dismiss()
                }
            }
        }
    }

    private fun setEditJourneyClickListener(): (editingJourney: Journey) -> View.OnClickListener {
        return fun(editingJourney: Journey): View.OnClickListener {
            return View.OnClickListener {
                val view: View = layoutInflater.inflate(R.layout.add_preset_journey_dialog, null)

                journeyNameET = view.findViewById(R.id.journeyNameET)
                startPointATV = view.findViewById(R.id.journeyStartATV)
                endPointATV = view.findViewById(R.id.journeyEndATV)

                journeyNameET?.text =
                    Editable.Factory.getInstance().newEditable(editingJourney.journeyName ?: "")
                startPointATV?.text =
                    Editable.Factory.getInstance().newEditable(editingJourney.sourceName ?: "")
                endPointATV?.text =
                    Editable.Factory.getInstance().newEditable(editingJourney.destinationName ?: "")

                var startPointChanged = false
                var endPointChanged = false

                // Extension function for delayed Search box autocomplete
                fun AutoCompleteTextView.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
                    this.addTextChangedListener(object : TextWatcher {
                        var timer: CountDownTimer? = null

                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int
                        ) {
                            // Not used
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                        override fun afterTextChanged(editable: Editable?) {
                            if (!isPerformingCompletion) {
                                timer?.cancel()
                                timer = object : CountDownTimer(1000, 1500) {
                                    override fun onTick(millisUntilFinished: Long) {}
                                    override fun onFinish() {
                                        afterTextChanged.invoke(editable.toString())
                                    }
                                }.start()
                            } else {
                                // Get the SearchResult of item that is selected from Mapbox
                                if (id == R.id.journeyStartATV) {
                                    // Find the search suggestion based on name
                                    val selectedStartSuggestion =
                                        loadedStartPlaces.firstOrNull { it.name == editable.toString() }

                                    searchRequestTask =
                                        selectedStartSuggestion?.let { it1 ->
                                            searchEngine.select(
                                                it1,
                                                searchCallback
                                            )
                                        }!!
                                } else {
                                    // Find the search suggestion based on name
                                    val selectedEndSuggestion =
                                        loadedEndPlaces.firstOrNull { it.name == editable.toString() }

                                    searchRequestTask =
                                        selectedEndSuggestion?.let { it1 ->
                                            searchEngine.select(
                                                it1,
                                                searchCallback
                                            )
                                        }!!
                                }
                            }
                        }
                    })
                }

                startPointATV?.afterTextChangedDelayed {
                    startPointChanged = true
                    searchEngine.search(
                        it,
                        SearchOptions(limit = 5),
                        searchCallback.apply { isEditingStartPoint = true })
                }

                endPointATV?.afterTextChangedDelayed {
                    endPointChanged = true
                    searchEngine.search(
                        it,
                        SearchOptions(limit = 5),
                        searchCallback.apply { isEditingStartPoint = false })
                }

                val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Edit ${editingJourney.journeyName ?: ""}")
                    .setNeutralButton("Cancel") { dialog, which ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton("Edit", null)
                    .setView(view)

                val addJourneyDialog = dialogBuilder.create()
                addJourneyDialog.show()

                // Override the button behaviour to prevent it from dismissing the dialog before passing all validation
                addJourneyDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    var passedValidation = true
                    startPointATV?.error = null
                    endPointATV?.error = null

                    // Respond to positive button press
                    if (journeyNameET?.text?.isBlank() == true) {
                        journeyNameET?.error = "Please provide journey name"
                        passedValidation = false
                    }

                    if (startPointChanged && selectedStart == null) {
                        startPointATV?.error = "Please provide a starting point for the journey"
                        passedValidation = false
                    }

                    if (endPointChanged && selectedEnd == null) {
                        endPointATV?.error = "Please provide an end point for the journey"
                        passedValidation = false
                    }

                    if (passedValidation) {
                        val startCoordinate = selectedStart?.coordinate
                        val endCoordinate = selectedEnd?.coordinate

                        editingJourney.apply {
                            this.journeyName = journeyNameET?.text.toString()
                            this.sourceName = selectedStart?.name ?: editingJourney.sourceName
                            this.sourceLat = startCoordinate?.latitude() ?: editingJourney.sourceLat
                            this.sourceLng =
                                startCoordinate?.longitude() ?: editingJourney.sourceLng
                            this.destinationName =
                                selectedEnd?.name ?: editingJourney.destinationName
                            this.destinationLat =
                                endCoordinate?.latitude() ?: editingJourney.destinationLat
                            this.destinationLng =
                                endCoordinate?.longitude() ?: editingJourney.destinationLng

                        }

                        viewModel?.update(editingJourney)
                        passedValidation = true
                    }

                    if (passedValidation) {
                        // Clear variables storing the values
                        selectedStart = null
                        selectedEnd = null

                        addJourneyDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun getAddressStr(address: SearchAddress?): String {
        if (address == null) {
            return ""
        }

        var locality = address.locality ?: ""
        var postcode = address.postcode ?: ""
        val country = address.country ?: ""

        if (locality.isNotBlank()) {
            locality += ", "
        }

        if (postcode.isNotBlank() and country.isNotBlank()) {
            postcode += ", "
        }

        return "$locality${address.region ?: ""} $postcode${address.country ?: ""}"
    }


    companion object {
        @JvmField
        val TAG = PresetJourneyFragment::class.java.simpleName
    }
}