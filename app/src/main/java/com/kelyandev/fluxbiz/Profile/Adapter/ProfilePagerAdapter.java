package com.kelyandev.fluxbiz.Profile.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kelyandev.fluxbiz.Profile.Fragments.BizzesFragment;
import com.kelyandev.fluxbiz.Profile.Fragments.RepliesFragment;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    private final Fragment[] fragments;

    public ProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments = new Fragment[]{
                new BizzesFragment(),
                new RepliesFragment()
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }
}
