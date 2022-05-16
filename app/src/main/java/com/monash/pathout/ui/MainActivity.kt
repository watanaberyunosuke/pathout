package com.monash.pathout.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.Style.Companion.MAPBOX_STREETS
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.ResponseInfo
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.feedback.SearchFeedbackBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import com.monash.pathout.MapAnnotationDelegate
import com.monash.pathout.R
import com.monash.pathout.ui.compass.CompassFragment
import com.monash.pathout.databinding.ActivityMainBinding
import com.monash.pathout.model.Stop
import com.monash.pathout.ui.login.LoginActivity
import com.monash.pathout.ui.login.UserProfileFragment
import com.monash.pathout.ui.preset_journey.PresetJourneyFragment
import com.monash.pathout.ui.report.ReportFragment
import com.monash.pathout.ui.transport_mode.TransportModeFragment
import com.monash.pathout.util.LocationPermissionHelper
import com.monash.pathout.viewmodel.FirebaseViewModel
import com.monash.pathout.viewmodel.JourneyViewModel
import com.monash.pathout.viewmodel.StopViewModel
import com.monash.pathout.work_manager.WorkManagerFragment
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    MapAnnotationDelegate {

    // navigation drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // maps
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapboxMap: MapboxMap
    private var mapStyle = MAPBOX_STREETS
    private var markerCoordinates = mutableListOf<Point>()
    private var transportAnnotationsManager: PointAnnotationManager? = null
    private var transportAnnotationRouteTypeOnMap: Int? = null

    // viewmodel
    private lateinit var stopViewModel: StopViewModel
    private lateinit var journeyViewModel: JourneyViewModel

    // bottom sheet
    private lateinit var searchBottomSheetView: SearchBottomSheetView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private lateinit var searchCategoriesView: SearchCategoriesBottomSheetView
    private lateinit var feedbackBottomSheetView: SearchFeedbackBottomSheetView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var cardsMediator: SearchViewBottomSheetsMediator

    // Update annotations on map whenever nearby stops for a specific PTV transport mode is loaded
    override suspend fun updateMapAnnotations(stops: List<Stop>) {
        for (stop in stops) {
            val point = Point.fromLngLat(stop.stopLongitude, stop.stopLatitude)
            addAnnotationToMap(point, stop.routeType)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        stopViewModel = ViewModelProvider(this)[StopViewModel::class.java]
        journeyViewModel = ViewModelProvider(this)[JourneyViewModel::class.java]

        // Configure Mapbox
        mapView = binding.mapView
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions { onMapReady() }
        initMapStyleFromSettings()

        // navigation
        drawerLayout = binding.drawerLayout
        toolbar = binding.toolbar2
        navigationView = binding.navigationView
        initNavigationDrawer()

        // bottom sheet
        configureBottomSheet()
        configureSearchBottomSheet(savedInstanceState)

        // Load user profile
        val firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        Firebase.auth.currentUser?.email?.let { firebaseViewModel.loadUserProfile(it) }
        firebaseViewModel.user.observe(this) {
            // Display username
            val userNameTextView = findViewById<TextView>(R.id.nav_header_username)
            userNameTextView.text = it.username

            // Load navigation method from shared preferences
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val navigationMethod = pref.getString("nav_method_key", "").toString()
            val travelMethodTextView = findViewById<TextView>(R.id.nav_header_travel_method)
            travelMethodTextView.text = "Preferred Travel Method: $navigationMethod"
        }
    }

    override fun onBackPressed() {
        // Bottom sheet
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN && !searchBottomSheetView.isHidden()) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            searchBottomSheetView.hide()
        }

        // Navigation drawer
        else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }

    private fun initMapStyleFromSettings() {
        val selectedMapStyle =
            PreferenceManager.getDefaultSharedPreferences(this).getString("main_map_style_key", "")

        val mapStyles = resources.getStringArray(R.array.map_styles)
        val mapStylesMapping = listOf(
            MAPBOX_STREETS,
            Style.OUTDOORS,
            Style.LIGHT,
            Style.DARK,
            Style.SATELLITE,
            Style.SATELLITE_STREETS,
            Style.TRAFFIC_DAY,
            Style.TRAFFIC_NIGHT
        )

        val idx = mapStyles.indexOf(selectedMapStyle)

        if (idx >= 0) {
            mapStyle = mapStylesMapping[idx]
        }
    }

    private fun initNavigationDrawer() {
        setSupportActionBar(toolbar)
        navigationView.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        // Left blank intentionally
        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private fun configureBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.bottomSheet.bottomSheetArrow.rotation = 180f
        binding.bottomSheet.bottomSheetArrow.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(
                bottomSheet: View,
                newState: Int
            ) {
                if (newState != BottomSheetBehavior.STATE_HIDDEN) {
                    searchBottomSheetView.hide()
                }
            } // Left Blank intentionally

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.bottomSheet.bottomSheetArrow.rotation = slideOffset * 180
            }
        })

        binding.bottomSheet.startJourneyBtn.setOnClickListener(View.OnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            searchBottomSheetView.expand()
        })

        replaceFragment(TransportModeFragment(), title.toString())
    }

    private fun onNavigatePlaceClicked(): (SearchPlace) -> Unit {
        return fun(place: SearchPlace) {
            val bundle = Bundle()
            val coords = doubleArrayOf(place.coordinate.latitude(), place.coordinate.longitude())

            bundle.putDoubleArray("DESTINATION", coords)
            bundle.putString("DESTINATION_NAME", place.name)

            Intent(this@MainActivity, NavigationActivity::class.java).let {
                it.putExtras(bundle)
                startActivity(it)
            }
        }
    }

    private fun configureSearchBottomSheet(savedInstanceState: Bundle?) {
        // Mapbox search sdk
        searchBottomSheetView = binding.searchView
        searchBottomSheetView.initializeSearch(
            savedInstanceState,
            SearchBottomSheetView.Configuration()
        )

        searchPlaceView = binding.searchPlaceView.apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.METRIC))

            isNavigateButtonVisible = true
            isShareButtonVisible = false
            isFavoriteButtonVisible = false
        }

        searchCategoriesView = binding.searchCategoriesView
        searchCategoriesView.initialize(CommonSearchViewConfiguration(DistanceUnitType.METRIC))

        feedbackBottomSheetView = binding.searchFeedbackView
        feedbackBottomSheetView.initialize(savedInstanceState)

        cardsMediator = SearchViewBottomSheetsMediator(
            searchBottomSheetView,
            searchPlaceView,
            searchCategoriesView,
            feedbackBottomSheetView,
            onNavigatePlaceClicked()
        )

        savedInstanceState?.let {
            cardsMediator.onRestoreInstanceState(it)
        }

        cardsMediator.addSearchBottomSheetsEventsListener(object :
            SearchViewBottomSheetsMediator.SearchBottomSheetsEventsListener {
            override fun onOpenPlaceBottomSheet(place: SearchPlace) {
                showMarker(place.coordinate)
            }

            override fun onOpenCategoriesBottomSheet(category: Category) {}

            override fun onBackToMainBottomSheet() {
                clearMarkers()

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                searchBottomSheetView.hide()
            }
        })

        searchCategoriesView.addCategoryLoadingStateListener(object :
            SearchCategoriesBottomSheetView.CategoryLoadingStateListener {
            override fun onLoadingStart(category: Category) {}

            override fun onCategoryResultsLoaded(
                category: Category,
                searchResults: List<SearchResult>,
                responseInfo: ResponseInfo,
            ) {
                showMarkers(searchResults.mapNotNull { it.coordinate })
            }

            override fun onLoadingError(category: Category, e: Exception) {}
        })

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }

        // Initially, hide the search bottom sheet
        searchBottomSheetView.hide()
    }

    private fun addAnnotationToMap(point: Point, routeType: Int) {
        val TRAIN = 0
        val TRAM = 1
        val BUS = 2
        val VLINE = 3
        val NIGHT_BUS = 4

        if (transportAnnotationRouteTypeOnMap != null && transportAnnotationRouteTypeOnMap != routeType) {
            if (transportAnnotationsManager != null) {
                transportAnnotationsManager!!.deleteAll()
            }
        }

        transportAnnotationRouteTypeOnMap = routeType

        var annotation = R.drawable.ic_baseline_train_24
        when (routeType) {
            TRAIN, VLINE -> annotation = R.drawable.ic_baseline_train_24
            TRAM -> annotation = R.drawable.ic_baseline_tram_24
            BUS, NIGHT_BUS -> annotation = R.drawable.ic_baseline_directions_bus_24
        }

        bitmapFromDrawableRes(
            this@MainActivity,
            annotation
        )?.let {
            val annotationApi = mapView.annotations

            if (transportAnnotationsManager == null) {
                transportAnnotationsManager = annotationApi.createPointAnnotationManager()
            }

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)

            transportAnnotationsManager!!.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    // Init mapview
    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )

        // Styling
        mapView.getMapboxMap().also { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.loadStyle(
                style(styleUri = getMapStyleUri()) {
                    +geoJsonSource(SEARCH_PIN_SOURCE_ID) {
                        featureCollection(
                            FeatureCollection.fromFeatures(
                                markerCoordinates.map {
                                    Feature.fromGeometry(it)
                                }
                            )
                        )
                    }
                    +image(SEARCH_PIN_IMAGE_ID) {
                        bitmap(createSearchPinDrawable().toBitmap(config = Bitmap.Config.ARGB_8888))
                    }
                    +symbolLayer(SEARCH_PIN_LAYER_ID, SEARCH_PIN_SOURCE_ID) {
                        iconImage(SEARCH_PIN_IMAGE_ID)
                        iconAllowOverlap(true)
                    }
                    initLocationComponent()
                    setGesturesListener()
                }
            )
        }

        // Hide linear progress indicator
        binding.mapProgressIndicator.hide()
    }

    private fun setGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    com.mapbox.maps.R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    com.mapbox.maps.R.drawable.mapbox_user_icon_shadow
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
    }


    private fun onCameraTrackingDismissed() {
        mapView.location.removeOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        mapView.location.removeOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    private fun replaceMainFragment(nextFragment: Fragment, title: String) {
        mapView.isVisible = false
        binding.mainFragmentContainerView.isVisible = true
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container_view, nextFragment)
            .commit()
        setTitle(title)
    }

    private fun replaceFragment(nextFragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, nextFragment)
            .commit()
        setTitle(title)
    }

    private fun getMapStyleUri(): String {
        val darkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return when (darkMode) {
            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_UNDEFINED -> mapStyle
            else -> error("Unknown night mode: $darkMode")
        }
    }

    private fun showMarkers(coordinates: List<Point>) {
        if (coordinates.isEmpty()) {
            clearMarkers()
            return
        } else if (coordinates.size == 1) {
            showMarker(coordinates.first())
            return
        }

        val cameraOptions = mapboxMap.cameraForCoordinates(
            coordinates, markersPaddings, bearing = null, pitch = null
        )

        if (cameraOptions.center == null) {
            clearMarkers()
            return
        }

        showMarkers(cameraOptions, coordinates)
    }

    private fun showMarker(coordinate: Point) {
        val cameraOptions = CameraOptions.Builder()
            .center(coordinate)
            .zoom(10.0)
            .build()

        showMarkers(cameraOptions, listOf(coordinate))
    }

    private fun showMarkers(cameraOptions: CameraOptions, coordinates: List<Point>) {
        markerCoordinates.clear()
        markerCoordinates.addAll(coordinates)
        updateMarkersOnMap()

        mapboxMap.setCamera(cameraOptions)
    }

    private fun clearMarkers() {
        markerCoordinates.clear()
        updateMarkersOnMap()
    }

    private fun updateMarkersOnMap() {
        mapboxMap.getStyle()?.getSourceAs<GeoJsonSource>(SEARCH_PIN_SOURCE_ID)?.featureCollection(
            FeatureCollection.fromFeatures(
                markerCoordinates.map {
                    Feature.fromGeometry(it)
                }
            )
        )
    }

    private companion object {
        const val SEARCH_PIN_SOURCE_ID = "search.pin.source.id"
        const val SEARCH_PIN_IMAGE_ID = "search.pin.image.id"
        const val SEARCH_PIN_LAYER_ID = "search.pin.layer.id"

        val markersPaddings: EdgeInsets = dpToPx(64).toDouble()
            .let { mapPadding ->
                EdgeInsets(mapPadding, mapPadding, mapPadding, mapPadding)
            }

        const val PERMISSIONS_REQUEST_LOCATION = 0
        private const val LOCATION_REQUEST_TIME_MS = 1000 * 6 * 1
        private val TAG = MainActivity::class.java.simpleName;

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun createSearchPinDrawable(): ShapeDrawable {
            val size = dpToPx(24)
            val drawable = ShapeDrawable(OvalShape())
            drawable.intrinsicWidth = size
            drawable.intrinsicHeight = size
            DrawableCompat.setTint(drawable, Color.RED)
            return drawable
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        when (item.itemId) {
            R.id.navigation_home -> {
                // Hide main fragment and show map with current location
                binding.mainFragmentContainerView.isVisible = false
                mapView.isVisible = true
                replaceFragment(TransportModeFragment(), item.title.toString())
            }
            R.id.navigation_journey -> searchBottomSheetView.expand()
            R.id.navigation_transport -> {
                searchBottomSheetView.hide()
                replaceFragment(TransportModeFragment(), item.title.toString())
            }

            R.id.navigation_saved -> {
                searchBottomSheetView.hide()
                replaceMainFragment(PresetJourneyFragment(), item.title.toString())
            }
            R.id.free_navigation -> startActivity(Intent(this, NavigationActivity::class.java))
            R.id.navigation_graph -> {
                replaceMainFragment(ReportFragment(), item.title.toString())
                replaceFragment(SelectGraphFragment(), item.title.toString())
            }

            R.id.navigation_settings -> replaceMainFragment(
                SettingsFragment(),
                item.title.toString()
            )
            R.id.set_backup -> replaceMainFragment(WorkManagerFragment(), item.title.toString())
            R.id.navigation_logout -> {
                Firebase.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                this.finish()
            }
            R.id.navigation_account -> replaceMainFragment(
                UserProfileFragment(),
                item.title.toString()
            )
            R.id.navigation_compass -> replaceMainFragment(CompassFragment(), item.title.toString())
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}






