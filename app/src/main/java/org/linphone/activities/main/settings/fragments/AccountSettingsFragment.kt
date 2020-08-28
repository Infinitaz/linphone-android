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
package org.linphone.activities.main.settings.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.linphone.R
import org.linphone.activities.main.settings.viewmodels.AccountSettingsViewModel
import org.linphone.activities.main.settings.viewmodels.AccountSettingsViewModelFactory
import org.linphone.activities.main.viewmodels.SharedMainViewModel
import org.linphone.core.tools.Log
import org.linphone.databinding.SettingsAccountFragmentBinding

class AccountSettingsFragment : Fragment() {
    private lateinit var binding: SettingsAccountFragmentBinding
    private lateinit var sharedViewModel: SharedMainViewModel
    private lateinit var viewModel: AccountSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsAccountFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = this

        sharedViewModel = activity?.run {
            ViewModelProvider(this).get(SharedMainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        val identity = arguments?.getString("Identity") ?: ""
        viewModel = ViewModelProvider(this, AccountSettingsViewModelFactory(identity)).get(AccountSettingsViewModel::class.java)
        binding.viewModel = viewModel

        binding.setBackClickListener { findNavController().popBackStack() }
        binding.back.visibility = if (resources.getBoolean(R.bool.isTablet)) View.INVISIBLE else View.VISIBLE

        viewModel.linkPhoneNumberEvent.observe(viewLifecycleOwner, {
            it.consume {
                if (findNavController().currentDestination?.id == R.id.accountSettingsFragment) {
                    val authInfo = viewModel.proxyConfig.findAuthInfo()
                    if (authInfo == null) {
                        Log.e("[Account Settings] Failed to find auth info for proxy config ${viewModel.proxyConfig}")
                    } else {
                        val args = Bundle()
                        args.putString("Username", authInfo.username)
                        args.putString("Password", authInfo.password)
                        args.putString("HA1", authInfo.ha1)
                        findNavController().navigate(
                            R.id.action_accountSettingsFragment_to_phoneAccountLinkingFragment,
                            args
                        )
                    }
                }
            }
        })

        viewModel.proxyConfigRemovedEvent.observe(viewLifecycleOwner, {
            it.consume {
                sharedViewModel.proxyConfigRemoved.value = true
                findNavController().navigateUp()
            }
        })
    }
}