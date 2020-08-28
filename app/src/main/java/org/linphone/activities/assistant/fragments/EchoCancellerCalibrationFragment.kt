/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities.assistant.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.linphone.activities.assistant.viewmodels.EchoCancellerCalibrationViewModel
import org.linphone.core.tools.Log
import org.linphone.databinding.AssistantEchoCancellerCalibrationFragmentBinding
import org.linphone.utils.PermissionHelper

class EchoCancellerCalibrationFragment : Fragment() {
    private lateinit var binding: AssistantEchoCancellerCalibrationFragmentBinding
    private lateinit var viewModel: EchoCancellerCalibrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AssistantEchoCancellerCalibrationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(EchoCancellerCalibrationViewModel::class.java)
        binding.viewModel = viewModel

        viewModel.echoCalibrationTerminated.observe(viewLifecycleOwner, {
            it.consume {
                requireActivity().finish()
            }
        })

        if (!PermissionHelper.required(requireContext()).hasRecordAudioPermission()) {
            Log.i("[Echo Canceller Calibration] Asking for RECORD_AUDIO permission")
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 0)
        } else {
            viewModel.startEchoCancellerCalibration()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val granted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (granted) {
            Log.i("[Echo Canceller Calibration] RECORD_AUDIO permission granted")
            viewModel.startEchoCancellerCalibration()
        } else {
            Log.w("[Echo Canceller Calibration] RECORD_AUDIO permission denied")
            requireActivity().finish()
        }
    }
}