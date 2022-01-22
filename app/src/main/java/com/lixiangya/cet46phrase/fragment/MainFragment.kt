package com.lixiangya.cet46phrase.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.lixiangya.cet46phrase.R
import com.lixiangya.cet46phrase.databinding.FragmentMainBinding
import com.lixiangya.cet46phrase.databinding.ItemPhraseSearchBinding
import com.lixiangya.cet46phrase.viewmodel.FragmentMainViewModel


class MainFragment : Fragment() {

    lateinit var dataBinding: FragmentMainBinding
    lateinit var viewModel: FragmentMainViewModel
    lateinit var unitAdapter: RecyclerView.Adapter<PhraseUnitViewHolder>
    lateinit var searchAdapter: RecyclerView.Adapter<SearchPhraseViewHolder>

    private var mRewardedAd: RewardedAd? = null
    val TAG = "main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FragmentMainViewModel::class.java)
        setHasOptionsMenu(true)


        val adRequest = AdRequest.Builder().build()

        val test = "ca-app-pub-3940256099942544/5224354917"
        val product = "ca-app-pub-3935377231710978/3797839318"
        RewardedAd.load(requireContext(),product, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "adLoad failure:"+adError.message)
                mRewardedAd = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                mRewardedAd = rewardedAd
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkInit()
        initView()
        initToolbar()
        initListener()
        onSubscribe()
        viewModel.load()
        checkFollow()
        unitAdapter.notifyDataSetChanged()

        mRewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad was shown.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                // Called when ad fails to show.
                Log.d(TAG, "Ad failed to show.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad was dismissed.")
                mRewardedAd = null
            }
        }

    }

    override fun onResume() {
        super.onResume()
        checkInit()
        checkFollow()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_main, menu)
        menu.findItem(R.id.menu_check_box_order).isChecked = viewModel.order
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_check_box_order -> {
                item.isChecked = !item.isChecked
                val edit =
                    requireContext().getSharedPreferences("config", Context.MODE_PRIVATE).edit()
                edit.putBoolean("order", item.isChecked)
                edit.apply()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {

        dataBinding.rvBoard.layoutManager = LinearLayoutManager(context)
        unitAdapter = object : RecyclerView.Adapter<PhraseUnitViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): PhraseUnitViewHolder {
                if (viewType == 1) {
                    val inflate =
                        layoutInflater.inflate(R.layout.item_phrase_category_title, parent, false)
                    return PhraseUnitViewHolder(inflate, 1)
                }
                val inflate = layoutInflater.inflate(R.layout.item_phrase_unit, parent, false)
                return PhraseUnitViewHolder(inflate, 2)
            }

            override fun onBindViewHolder(holder: PhraseUnitViewHolder, position: Int) {
                var mPosition = position
                var itemType: String? = null
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    if (mPosition < viewModel.verbUnitList!!.size) {
                        itemType = "动词词组"
                    } else {
                        mPosition -= viewModel.verbUnitList!!.size
                    }
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty() && itemType == null) {
                    if (mPosition < viewModel.prepUnitList!!.size) {
                        itemType = "介词词组"
                    } else {
                        mPosition -= viewModel.prepUnitList!!.size
                    }
                }

                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty() && itemType == null) {
                    if (mPosition < viewModel.otherUnitList!!.size) {
                        itemType = "其他词组"
                    }
                }

                if (getItemViewType(position) == 1) {
                    if (itemType == null) {
                        itemType = "词组"
                    }
                    holder.title?.text = itemType
                    return
                }
                mPosition = position
                var itemUnit: String? = null
                holder.itemView.isClickable = false
                holder.checkBox?.visibility = View.GONE
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    mPosition--
                    if (mPosition < viewModel.verbUnitList!!.size) {
                        itemUnit = viewModel.verbUnitList!![mPosition]
                    } else {
                        mPosition -= viewModel.verbUnitList!!.size
                    }
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty() && itemUnit == null) {
                    mPosition--
                    if (mPosition < viewModel.prepUnitList!!.size) {
                        itemUnit = viewModel.prepUnitList!![mPosition]
                    } else {
                        mPosition -= viewModel.prepUnitList!!.size
                    }
                }

                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty() && itemUnit == null) {
                    mPosition--
                    if (mPosition < viewModel.otherUnitList!!.size) {
                        itemUnit = viewModel.otherUnitList!![mPosition]
                    } else {
                        mPosition -= viewModel.otherUnitList!!.size
                    }
                }

                holder.unit?.text = itemUnit
            }

            override fun getItemCount(): Int {
                var count = 0
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    count += viewModel.verbUnitList!!.size + 1
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()) {
                    count += viewModel.prepUnitList!!.size + 1
                }
                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()) {
                    count += viewModel.otherUnitList!!.size + 1
                }
                return count
            }

            override fun getItemViewType(position: Int): Int {
                var mPosition = position
                if (viewModel.verbUnitList != null && viewModel.verbUnitList!!.isNotEmpty()) {
                    if (mPosition == 0) {
                        return 1
                    }
                    mPosition = mPosition - viewModel.verbUnitList!!.size - 1
                }
                if (viewModel.prepUnitList != null && viewModel.prepUnitList!!.isNotEmpty()) {
                    if (mPosition == 0) {
                        return 1
                    }
                    mPosition = mPosition - viewModel.prepUnitList!!.size - 1
                }

                if (viewModel.otherUnitList != null && viewModel.otherUnitList!!.isNotEmpty()) {
                    if (mPosition == 0) {
                        return 1
                    }
                    mPosition = mPosition - viewModel.otherUnitList!!.size - 1
                }

                return 2
            }
        }
        dataBinding.rvBoard.adapter = unitAdapter

        dataBinding.rvSearchPhrase.layoutManager = LinearLayoutManager(context)
        searchAdapter = object : RecyclerView.Adapter<SearchPhraseViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): SearchPhraseViewHolder {
                val dataBinding = DataBindingUtil.inflate<ItemPhraseSearchBinding>(
                    layoutInflater,
                    R.layout.item_phrase_search,
                    parent,
                    false
                )
                return SearchPhraseViewHolder(dataBinding)
            }

            override fun onBindViewHolder(holder: SearchPhraseViewHolder, position: Int) {
                if (viewModel.searchPhrasesLiveData.value == null) return
                val phrase = viewModel.searchPhrasesLiveData.value!![position]
                val dataBinding = holder.dataBinding
                dataBinding.tvPhrase.text = phrase.phrase
                dataBinding.root.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putSerializable("phrase", phrase)
                    findNavController().navigate(R.id.action_mainFragment_to_learnFragment, bundle)
                }
            }

            override fun getItemCount(): Int {
                return if (viewModel.searchPhrasesLiveData.value == null) 0 else viewModel.searchPhrasesLiveData.value!!.size
            }

        }
        dataBinding.rvSearchPhrase.adapter = searchAdapter
    }

    private fun initToolbar() {
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val mDrawerToggle = ActionBarDrawerToggle(
            activity,
            dataBinding.drawerLayout,
            dataBinding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        dataBinding.drawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
    }

    private fun initListener() {
        dataBinding.btnEdit.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_selectFragment) }
        dataBinding.btnAction.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_learnFragment) }
        dataBinding.btnReview.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isReviewMode", true)
            findNavController().navigate(R.id.action_mainFragment_to_learnFragment, bundle)
        }
        dataBinding.btnPhraseList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_phraseListFragment)
        }

        dataBinding.searchView.setIconifiedByDefault(false)
        dataBinding.searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            Log.i("main", "searchView.hasFocus:$hasFocus")
            dataBinding.searchView
            if (hasFocus) {
                dataBinding.blockSearchList.visibility = View.VISIBLE
            } else {
                dataBinding.blockSearchList.visibility = View.GONE
                //TODO close keyboard
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
        dataBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotEmpty()) {
                    viewModel.searchPhraseByKeyWord(newText)
                } else {
                    viewModel.searchPhrasesLiveData.value = null
                }

                return true
            }
        })

        dataBinding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_item_ad -> {
                    if (mRewardedAd != null) {
                        Log.i(TAG, "initListener: showAD")
                        mRewardedAd?.show(requireActivity(), OnUserEarnedRewardListener() {
                            fun onUserEarnedReward(rewardItem: RewardItem) {
                                var rewardAmount = rewardItem.amount
                                var rewardType = rewardItem.type
                                Log.d(TAG, "User earned the reward.")
                            }
                        })
                    } else {
                        Toast.makeText(context,"Thank you for supporting us,Something is wrong, please retry again later!",Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "The rewarded ad wasn't ready yet.")
                    }
                }
                R.id.menu_item_notes -> {
                    findNavController().navigate(R.id.noteListFragment)
                }
                R.id.menu_item_setting -> {
                    findNavController().navigate(R.id.settingsFragment)
                }
            }
            true
        }
    }

    private fun onSubscribe() {
        viewModel.searchPhrasesLiveData.observe(viewLifecycleOwner) {
            searchAdapter.notifyDataSetChanged()
        }
    }

    private fun checkInit() {
        Log.d("main", "check() called")
        val sharedPreferences =
            requireContext().getSharedPreferences("config", Context.MODE_PRIVATE)
        val init = sharedPreferences.getBoolean("init", false)
        if (!init) {
            findNavController().navigate(R.id.action_mainFragment_to_loadDataFragment)
        }
    }

    private fun checkFollow() {
        if ((viewModel.verbUnitList == null || viewModel.verbUnitList!!.isEmpty()) && (viewModel.prepUnitList == null || viewModel.prepUnitList!!.isEmpty()) && (viewModel.otherUnitList == null || viewModel.otherUnitList!!.isEmpty())) {
            if (findNavController().currentDestination?.id == R.id.mainFragment) {
                findNavController().navigate(R.id.action_mainFragment_to_selectFragment)
            }
        }
    }


    inner class SearchPhraseViewHolder(val dataBinding: ItemPhraseSearchBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    inner class PhraseUnitViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView) {
        var type: TextView? = null
        var unit: TextView? = null
        var progress: ProgressBar? = null
        var flag: ImageView? = null
        var title: TextView? = null
        var checkBox: CheckBox? = null

        init {
            if (viewType == 2) {
                type = itemView.findViewById(R.id.tv_type)
                unit = itemView.findViewById(R.id.tv_unit)
                progress = itemView.findViewById(R.id.progressBar_unit)
                flag = itemView.findViewById(R.id.iv_unit_flag)
                checkBox = itemView.findViewById(R.id.checkbox)
            } else if (viewType == 1) {
                title = itemView.findViewById(R.id.tv_title)
            }
        }
    }


}