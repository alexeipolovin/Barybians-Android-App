package ru.maxim.barybians.ui.fragment.dialogsList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.fragment_dialogs_list.*
import ru.maxim.barybians.R
import ru.maxim.barybians.model.Dialog
import ru.maxim.barybians.ui.activity.dialog.DialogActivity
import ru.maxim.barybians.utils.toast

class DialogsListFragment : MvpAppCompatFragment(), DialogsListView {

    @InjectPresenter
    lateinit var dialogsListPresenter: DialogsListPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_dialogs_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogsListRefreshLayout.setOnRefreshListener { dialogsListPresenter.loadDialogsList() }
    }

    override fun showDialogsList(dialogsList: ArrayList<Dialog>) {
        dialogsListRefreshLayout.isRefreshing = false
        dialogsListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DialogsListRecyclerAdapter(dialogsList) { userId ->
                startActivity(
                    Intent(context, DialogActivity::class.java)
                        .apply { putExtra("userId", userId) }
                )
            }
        }
    }

    override fun onDialogsListLoadError() {
        context?.toast(R.string.an_error_occurred_while_loading_dialogs)
    }
}