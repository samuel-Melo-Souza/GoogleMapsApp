package com.example.googlemapsapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.googlemapsapp.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    //Objeto que será chamado a cada atualização de localização
    private LocationCallback localAtualizado;
    //Objeto para configuração de utilização do GPS
    private LocationRequest configuracaoGPS;

    //Objeto da classe FusedLocation para iniciar o serviço de GPS
    private FusedLocationProviderClient servicoLocalizacao;

    //Objeto para armazenar a última localização recebida
    private Location ultimaLocalizacao;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Iniciar o serviço de localização (FusedLocation)
        servicoLocalizacao = LocationServices.getFusedLocationProviderClient(this);

        //Adicionar as configurações do GPS
        configuracaoGPS = new LocationRequest();
        configuracaoGPS.setInterval(10000); //Intervalo padrão de atualização (10 seg)
        configuracaoGPS.setFastestInterval(20000); //Intervalo de atualização (2 seg)
        //Uso da bateria (precisão da localização)
        configuracaoGPS.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Função callback a ser chamada a cada atualização/mudança de localização
        localAtualizado = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                //Testar se há um valor válido de localização
                if (locationResult == null){
                    return; //Cancela a atualização se não há um valor válido
                }
                //Quando há um valor válido...
                for (Location localValido : locationResult.getLocations()){
                    //Move o mapa para a localização encontrada
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(
                            localValido.getLatitude(),
                            localValido.getLatitude()), 15));
                    //Atualizar o objeto "UltimaLocalizaçao"
                    ultimaLocalizacao = new Location(LocationManager.FUSED_PROVIDER);
                    ultimaLocalizacao.setLatitude(localValido.getLatitude());
                    ultimaLocalizacao.setLongitude(localValido.getLatitude());
                    //Adicionar marcador na posição
                    mMap.addMarker(new MarkerOptions().position(new LatLng(localValido.getLatitude(),
                            localValido.getLongitude())).title("Localização"));
                }
            }
        };

        //Verificar e requisitar a permissão para uso do GPS
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }

        //Configurar o serviço de localização com o callback e configurações
        servicoLocalizacao.requestLocationUpdates(configuracaoGPS, localAtualizado, Looper.getMainLooper());

        //Verificar se o GPS está ativado
        LocationManager gpsAtivado = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!gpsAtivado.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Se não estiver ativado, direciona para a tela de configurações
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            Toast.makeText(this, "É necessário ativar o GPS",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}