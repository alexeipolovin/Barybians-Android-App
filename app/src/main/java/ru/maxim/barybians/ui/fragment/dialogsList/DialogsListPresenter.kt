package ru.maxim.barybians.ui.fragment.dialogsList

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ru.maxim.barybians.model.Dialog
import ru.maxim.barybians.repository.remote.service.DialogService

@InjectViewState
class DialogsListPresenter : MvpPresenter<DialogsListView>(), CoroutineScope by MainScope() {

    private val dialogService = DialogService()

    fun loadDialogsList() {
        launch {
            try {
                val dialogs = dialogService.getDialogsList()
                if (dialogs.isSuccessful && dialogs.body() != null) {
                    val dialogList = ArrayList<Dialog>()
                    dialogs.body()?.entrySet()?.forEach {
                        dialogList.add(Gson().fromJson(it.value, Dialog::class.java))
                    }
                    dialogList.sortByDescending { dialog -> dialog.lastMessage.time }
                    viewState.showDialogsList(dialogList)
                }
            } catch (e: Exception) {

            }
        }
    }
}