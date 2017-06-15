package com.example.oliver.planet;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * Created by Oliver on 6/15/2017.
 */

public class DialogEditPlanet extends FrameLayout {

    // Planet to edit
    private Planet m_planet;

    // Controls
    SeekBar m_sizeSlider;
    Spinner m_typeSpinner;

    View m_view;

    public DialogEditPlanet(Context context, Planet p) {
        super(context);

        m_planet = p;

        m_view = inflate(context, R.layout.dlg_edit_planet, null);

        this.addView(m_view);

        m_sizeSlider = (SeekBar) m_view.findViewById(R.id.planetSizeSlider);
        m_typeSpinner = (Spinner) m_view.findViewById(R.id.planetTypeSpinner);

        CharSequence[] options = {"Planet", "Repulsar", "Sun", "Black Hole"};

        // Add planet options
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, options);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        m_typeSpinner.setAdapter(adapter);
    }

    // Load planet settings into the dialog
    public void loadPlanetSettings() {

        m_sizeSlider.setMax((int) (m_planet.getMaxAllowableRadius() - m_planet.getMinAllowableRadius()));
        m_sizeSlider.setProgress((int) (m_planet.getRadius() - m_planet.getMinAllowableRadius()));

        switch (m_planet.getPlanetType()) {
            case MOON:
                // Moon type cannot be changed
                m_typeSpinner.setVisibility(INVISIBLE);
                break;
            default:
                m_typeSpinner.setVisibility(VISIBLE);
                break;
        }
    }

    // Save new planet settings
    public void savePlanetSettings() {

        m_planet.setRadius(m_sizeSlider.getProgress() + m_planet.getMinAllowableRadius());
    }
}
