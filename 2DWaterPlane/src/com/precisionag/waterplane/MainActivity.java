package com.precisionag.waterplane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.precisionag.lib.CustomMarker;
import com.precisionag.lib.ElevationRaster;
import com.precisionag.lib.Field;
import com.precisionag.lib.MyMapFragment;
import com.precisionag.lib.ReadGridFloatTask;

public class MainActivity extends Activity implements OnMapClickListener, OnCameraChangeListener, OnMarkerDragListener, OnTouchListener {
private static final int ADD_MODE = 1;
private static final int DRAG_MODE = 2;

GroundOverlay prevoverlay;
static Field field;
static List<CustomMarker> markers;
LatLng userLocation;
int mode;
double waterLevelMeters;
LocationManager locationManager;
Context context = this;
Marker userMarker;
private Uri fileUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.custom_ab);

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.field);
		MyMapFragment mapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map);
		GoogleMap map = mapFragment.getMap();
		map.setOnCameraChangeListener(this);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		UiSettings uiSettings = map.getUiSettings();
		
		userMarker = map.addMarker(new MarkerOptions()
        .position(new LatLng(0, 0))
        .title("You are here"));
		
		map.setOnMarkerDragListener(this);

		
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setTiltGesturesEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		
		field = new Field(bitmap, new LatLng(0.0, 0.0), new LatLng(0.0, 0.0), 0.0, 0.0);
		userLocation = new LatLng(0.0, 0.0);
		markers = new ArrayList<CustomMarker>();
		mode = 0;
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		
		CustomMarker.setDisplayWidth(width);
		CustomMarker.setField(field);
		CustomMarker.setMap(map);
		CustomMarker.setContext(context);
		CustomMarker.setLayout((RelativeLayout)findViewById(R.id.TopLevelView));
		RelativeLayout lay = (RelativeLayout) findViewById(R.id.TopLevelView);
		lay.setOnTouchListener(this);
		
		readDataFile(field);
		prevoverlay = field.createOverlay(map);
		configSeekbar(field, prevoverlay);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		//set button listeners
		final Button buttonPlus = (Button) findViewById(R.id.buttonPlus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Increase elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()+2);
            	updateColors(field);
            }
        });
        
        final Button buttonMinus = (Button) findViewById(R.id.buttonMinus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Decrease elevation
            	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            	seekBar.setProgress(seekBar.getProgress()-2);
            	updateColors(field);
            }
        });
        
        final Button buttonDelete = (Button) findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Iterator<CustomMarker> i = markers.iterator();
            	CustomMarker marker;

            	while (i.hasNext()) {
            		 marker = i.next();
            		 if (CustomMarker.getSelected() == marker.getButton()) {
            			 marker.removeMarker();
            			 markers.remove(marker);
            			 break;
            		 }
            	}
            	
                CustomMarker.getLayout().removeView(CustomMarker.getSelected());
            }
        });
        
        final Button buttonOpenDem = (Button) findViewById(R.id.buttonOpenDem);
        buttonOpenDem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//opens file manager
            	Intent intent = new Intent("org.openintents.action.PICK_FILE");
            	intent.setData(Uri.parse("file:///sdcard/dem"));
            	startActivityForResult(intent, 1);
            }
        });
		
		updateColors(field);
		
		
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			if (mode != DRAG_MODE) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						context);
		 
					// set title
					alertDialogBuilder.setTitle("GPS is not enabled");
		 
					// set dialog message
					alertDialogBuilder
						.setMessage("Please enable GPS!")
						.setCancelable(false)
						.setPositiveButton("Exit",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// if this button is clicked, close
								// current activity
								MainActivity.this.finish();
							}
						  })
						.setNegativeButton("GPS Settings",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						});
		 
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
		 
						// show it
						alertDialog.show();
			}
		}
		
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		MenuItem item = menu.findItem(R.id.menu_drag);
		if (mode == DRAG_MODE) {
			item.setIcon(R.drawable.unlock);
		}
		else {
			item.setIcon(R.drawable.lock);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    if (item.getItemId() == R.id.item_legal) {
	        	  String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(
	              getApplicationContext());
	              AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(MainActivity.this);
	              LicenseDialog.setTitle("Legal Notices");
	              LicenseDialog.setMessage(LicenseInfo);
	              LicenseDialog.show();
	        	  return true;
	    }
	    else if (item.getItemId() == R.id.menu_add) {
        	mode = ADD_MODE;
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_drag) {
	    	if (mode == DRAG_MODE) {
	    		mode = 0;
	    		userLocation = new LatLng(
	    				locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(), 
	    				locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
	    		
	    	  double elevationDouble = field.elevationFromLatLng(userLocation);
	  		  double elevationDelta =  elevationDouble - waterLevelMeters;
	  		  String ElevationText;
	  		  TextView ElevationTextView = (TextView) findViewById(R.id.text2);
	  		  
	  		  if (elevationDouble == 0.0) {
	  			  ElevationText = "You are not in the field.";
	  		  }
	  		  else {
	  		  	  String elevationString = new DecimalFormat("#.#").format(Math.abs(elevationDouble));
	  		  	  String elevationDeltaString = new DecimalFormat("#.#").format(Math.abs(elevationDelta));
	  		  	  if (elevationDelta >= 0.0) {
	  		  		  ElevationText = "Your Elevation: " + elevationDeltaString + "m above water (" + elevationString + "m)";
	  		  	  }
	  		  	  else {
	  		  		ElevationText = "Your Elevation: " + elevationDeltaString + "m below water (" + elevationString + "m)";
	  		  	  }
	  		  }
	  		  ElevationTextView.setText(ElevationText);
	  		  
	  		  CustomMarker.setUserElevation(elevationDouble);
	  		  userMarker.setPosition(userLocation);
	    	} 
	    	else {
	    		mode = DRAG_MODE;
	    		
	    	}
	    	userMarker.setDraggable(mode == DRAG_MODE);
	    	
	    	invalidateOptionsMenu();
	    	
            return true;
	    }
	    else {
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onMapClick (LatLng point) {
		CustomMarker.setSelected(null);
		updateMarkers();
		switch(mode) {
			case ADD_MODE:
				CustomMarker.setWaterElevation(waterLevelMeters);
				CustomMarker newMarker = new CustomMarker(point);
				newMarker.updateMarker();
				markers.add(newMarker);
				updateMarkers();
				mode = 0;
				break;
				
			default:
				break;
		}
		
	}
	
//takes a bitmap, latitude/longitude bounds, and a map to create a map overlay
//this has been duplicated in the Field class
private GroundOverlay createOverlay(Bitmap overlayBitmap, LatLngBounds bounds) {
	MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
	BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(overlayBitmap);
	GoogleMap map = mapFragment.getMap();
	GroundOverlay groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
     .image(image)
     .positionFromBounds(bounds)
     .transparency(0));
	groundOverlay.setVisible(true);
	return groundOverlay;
}
	
public void updateColors(Field field) {
	//get level from seekbar
	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	seekBar.setMax(255);
	int waterLevel = seekBar.getProgress();
	
	int width = field.getElevationBitmap().getWidth();
	int height = field.getElevationBitmap().getHeight();
	int[] pixels = new int[width * height];
	field.getElevationBitmap().getPixels(pixels, 0, width, 0, 0, width, height);
	Bitmap bitmap = field.getElevationBitmap().copy(field.getElevationBitmap().getConfig(), true);
	
	//test each pixel, if below water level set blue, else set transparent
	for (int i = 0; i < (width * height); i++) {
		if ((pixels[i] & 0x000000FF) < waterLevel) {
			//water is visible, set pixel to blue
			pixels[i] = 0xFF0000FF;
		} else {
			//no water, set pixel transparent
			pixels[i] = 0x00000000;
		}
	}
	bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
	
	//remove old map overlay and create new one
	prevoverlay.remove();

	prevoverlay = createOverlay(bitmap, field.getFieldBounds());

}

private void configSeekbar(final Field field, final GroundOverlay overlay) {
	SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	seekBar.setMax(255);
	seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
		@Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (userLocation != null ) {
				//get level from seekbar
				int waterLevel = seekBar.getProgress();
				
				//update text block
				waterLevelMeters = field.getMinElevation() + ((double)waterLevel*(field.getMaxElevation()-field.getMinElevation())/255.0);
				TextView waterElevationTextView = (TextView) findViewById(R.id.text);
				String elevation = new DecimalFormat("#.#").format(waterLevelMeters);
				String waterElevationText = "Elevation: " + elevation + "m";
				waterElevationTextView.setText(waterElevationText);
				
				//update other text block
				  double elevationDouble = field.elevationFromLatLng(userLocation);
				  double elevationDelta =  elevationDouble - waterLevelMeters;
				  String ElevationText;
				  TextView ElevationTextView = (TextView) findViewById(R.id.text2);
				  
				  if (elevationDouble == 0.0) {
					  ElevationText = "You are not in the field.";
				  }
				  else {
				  	  String elevationString = new DecimalFormat("#.#").format(Math.abs(elevationDouble));
				  	  String elevationDeltaString = new DecimalFormat("#.#").format(Math.abs(elevationDelta));
				  	  if (elevationDelta >= 0.0) {
				  		  ElevationText = "Your Elevation: " + elevationDeltaString + "m above water (" + elevationString + "m)";
				  	  }
				  	  else {
				  		ElevationText = "Your Elevation: " + elevationDeltaString + "m below water (" + elevationString + "m)";
				  	  }
				  }
				  ElevationTextView.setText(ElevationText);
				  
				  //update marker text
				  CustomMarker.setWaterElevation(waterLevelMeters);
				  				  
				  //visual updates
				  updateMarkers();
				  updateColors(field);
			}
			
		}
		
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        	updateColors(field);
        	updateMarkers();
        }
	});
	seekBar.setProgress(seekBar.getMax()/2);
}

public static void updateMarkers() {
	Iterator<CustomMarker> i = markers.iterator();
	CustomMarker marker;

	while (i.hasNext()) {
		 marker = i.next();
		 marker.updateMarker();
	}
}

private void readDataFile(Field field) {
	try {
		
		//read data from string
		AssetManager am = getApplicationContext().getAssets();
		BufferedReader dataIO = new BufferedReader(new InputStreamReader(am.open("field.latlng")));
	    String dataString = null;

	    dataString = dataIO.readLine();
	    double north = Double.parseDouble(dataString);
	    dataString = dataIO.readLine();
	    double east = Double.parseDouble(dataString);
	    dataString = dataIO.readLine();
	    double south = Double.parseDouble(dataString);
	    dataString = dataIO.readLine();
	    double west = Double.parseDouble(dataString);
	    
	    LatLng northEast = new LatLng(north, east);
	    LatLng southWest = new LatLng(south, west);
	    
	    dataString = dataIO.readLine();
	    double minElevation = Double.parseDouble(dataString);
	    dataString = dataIO.readLine();
	    double maxElevation = Double.parseDouble(dataString);
	    
	    //set corresponding parameters in field
	    field.setBounds(new LatLngBounds(northEast, southWest));
	    field.setNortheast(northEast);
	    field.setSouthwest(southWest);
	    field.setMinElevation(minElevation);
	    field.setMaxElevation(maxElevation);
	    
	    dataIO.close();
	    
	
	}
	catch  (IOException e) {
	}


}

LocationListener locationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
      // Called when a new location is found by the network location provider.
    	if (mode != DRAG_MODE) {

	      userLocation = new LatLng(location.getLatitude(), location.getLongitude());
	      
		  double elevationDouble = field.elevationFromLatLng(userLocation);
		  double elevationDelta =  elevationDouble - waterLevelMeters;
		  String ElevationText;
		  TextView ElevationTextView = (TextView) findViewById(R.id.text2);
		  
		  if (elevationDouble == 0.0) {
			  ElevationText = "You are not in the field.";
		  }
		  else {
		  	  String elevationString = new DecimalFormat("#.#").format(Math.abs(elevationDouble));
		  	  String elevationDeltaString = new DecimalFormat("#.#").format(Math.abs(elevationDelta));
		  	  if (elevationDelta >= 0.0) {
		  		  ElevationText = "Your Elevation: " + elevationDeltaString + "m above water (" + elevationString + "m)";
		  	  }
		  	  else {
		  		ElevationText = "Your Elevation: " + elevationDeltaString + "m below water (" + elevationString + "m)";
		  	  }
		  }
		  ElevationTextView.setText(ElevationText);
		  
		  CustomMarker.setUserElevation(elevationDouble);
		  userMarker.setPosition(userLocation);
    	}
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}
  };

@Override
public void onCameraChange(CameraPosition position) {
	updateMarkers();
}

@Override
public void onMarkerDrag(Marker marker) {
	// TODO Auto-generated method stub
	userLocation = marker.getPosition();
	double elevationDouble = field.elevationFromLatLng(userLocation);
	  double elevationDelta =  elevationDouble - waterLevelMeters;
	  String ElevationText;
	  TextView ElevationTextView = (TextView) findViewById(R.id.text2);
	  
	  if (elevationDouble == 0.0) {
		  ElevationText = "You are not in the field.";
	  }
	  else {
	  	  String elevationString = new DecimalFormat("#.#").format(Math.abs(elevationDouble));
	  	  String elevationDeltaString = new DecimalFormat("#.#").format(Math.abs(elevationDelta));
	  	  if (elevationDelta >= 0.0) {
	  		  ElevationText = "Your Elevation: " + elevationDeltaString + "m above water (" + elevationString + "m)";
	  	  }
	  	  else {
	  		ElevationText = "Your Elevation: " + elevationDeltaString + "m below water (" + elevationString + "m)";
	  	  }
	  }
	  ElevationTextView.setText(ElevationText);
	  
	  CustomMarker.setUserElevation(elevationDouble);
	  updateMarkers();
}

@Override
public void onMarkerDragEnd(Marker marker) {
	// TODO Auto-generated method stub
	
}

@Override
public void onMarkerDragStart(Marker marker) {
	// TODO Auto-generated method stub
	
}

@Override
public boolean onTouch(View arg0, MotionEvent arg1) {
	updateMarkers();
	return false;
}

protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	//handle data from file manager
	fileUri = data.getData();
	java.net.URI juri = null;
	try {
		juri = new java.net.URI(fileUri.getScheme(),
		        fileUri.getSchemeSpecificPart(),
		        fileUri.getFragment());
	} catch (URISyntaxException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	ElevationRaster raster = new ElevationRaster();
	new ReadGridFloatTask(this, raster).execute(juri);
}

public void onFileRead() {
	
}

}