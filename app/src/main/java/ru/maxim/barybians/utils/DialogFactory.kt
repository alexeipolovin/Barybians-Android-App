package ru.maxim.barybians.utils

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_comments_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_edit_status.*
import kotlinx.android.synthetic.main.fragment_edit_status.view.*
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_likes_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_post_editor.view.*
import kotlinx.android.synthetic.main.fragment_post_menu_bottom_sheet.*
import ru.maxim.barybians.R
import ru.maxim.barybians.model.response.CommentResponse
import ru.maxim.barybians.repository.local.PreferencesManager
import ru.maxim.barybians.ui.fragment.base.PostItem
import ru.maxim.barybians.ui.fragment.base.PostItem.CommentItem
import ru.maxim.barybians.ui.fragment.base.PostItem.UserItem
import ru.maxim.barybians.ui.fragment.profile.CommentsRecyclerAdapter
import ru.maxim.barybians.ui.fragment.profile.LikedUsersRecyclerAdapter


/**
 * Singleton class for create dialogs
 */
object DialogFactory {

    fun createLikesListDialog(
        likes: ArrayList<UserItem>,
        onUserClick: (userId: Int) -> Unit
    ) = LikesBottomSheetFragment.newInstance(likes, onUserClick)

    fun createCommentsListDialog(
        comments: ArrayList<CommentItem>,
        onUserClick: (userId: Int) -> Unit,
        onImageClick: (drawable: Drawable) -> Unit,
        htmlParser: HtmlParser,
        addCommentCallback: (text: String) -> Unit,
        deleteCommentCallback: (commentPosition: Int, commentId: Int) -> Unit
    ) = CommentBottomSheetFragment.newInstance(
            comments,
            onUserClick,
            onImageClick,
            addCommentCallback,
            deleteCommentCallback,
            htmlParser)

    fun createEditStatusDialog(
        status: String?,
        editCallback: (status: String?) -> Unit
    ) =
        EditStatusDialogFragment.newInstance(status, editCallback)


    fun createPostMenu(
        title: String?,
        text: String,
        onDelete: () -> Unit,
        onEdit: (title: String?, text: String) -> Unit
    ) = PostMenuBottomSheetFragment.newInstance(title, text, onDelete, onEdit)


    class LikesBottomSheetFragment : BottomSheetDialogFragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? = inflater.inflate(R.layout.fragment_likes_bottom_sheet, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val likesCount = likes.size
            if (likesCount == 0) {
                likesBottomSheetTitle.visibility = View.GONE
                likesBottomSheetMessage.text = context?.getString(R.string.nobody_like_this)
            } else {
                likesBottomSheetMessage.visibility = View.GONE
                likesBottomSheetTitle.text =
                    context?.resources?.getQuantityString(
                        R.plurals.like_plurals,
                        likesCount,
                        likesCount
                    )
            }
            likesBottomSheetRecyclerView.let {
                it.layoutManager = LinearLayoutManager(context)
                it.adapter = LikedUsersRecyclerAdapter(likes) {userId ->
                    onUserClick(userId)
                    dismiss()
                }
            }
        }

        companion object {
            private lateinit var likes: ArrayList<UserItem>
            private lateinit var onUserClick: (userId: Int) -> Unit

            fun newInstance(likes: ArrayList<UserItem>,
                            onUserClick: (userId: Int) -> Unit
            ): LikesBottomSheetFragment {
                this.likes = likes
                this.onUserClick = onUserClick
                return LikesBottomSheetFragment()
            }
        }
    }

    class CommentBottomSheetFragment : BottomSheetDialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? = inflater.inflate(R.layout.fragment_comments_bottom_sheet, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val commentsCount = comments.size
            if (commentsCount == 0) {
                commentsBottomSheetTitle.text = context?.getString(R.string.no_comments_yet)
            } else {
                commentsBottomSheetTitle.text =
                    context?.resources?.getQuantityString(
                        R.plurals.comment_plurals,
                        commentsCount,
                        commentsCount
                    )
            }
            commentsBottomSheetRecyclerView.let {
                it.layoutManager = LinearLayoutManager(context)
                it.adapter = CommentsRecyclerAdapter(
                    comments,
                    { userId ->
                        onUserClick(userId)
                        dismiss()
                    },
                    onImageClick,
                    deleteCommentCallback,
                    htmlParser
                )
            }

            commentsBottomSheetEditor.addTextChangedListener {
                val buttonResource =
                    if (it.isNullOrBlank()) R.drawable.ic_send_grey
                    else R.drawable.ic_send_blue
                commentsBottomSheetSend.setBackgroundResource(buttonResource)
                commentsBottomSheetEditor.requestFocus()
            }
            commentsBottomSheetSend.apply {
                setBackgroundResource(R.drawable.ic_send_grey)
                setOnClickListener {
                    val text = commentsBottomSheetEditor.text
                    if (!text.isNullOrBlank()) {
                        addCommentCallback(text.toString())
                    }
                }
            }
        }

        fun addComment(comment: CommentResponse) {
            val date = DateFormatUtils.getSimplifiedDate(comment.date*1000)
            val author = UserItem(
                PreferencesManager.userId,
                PreferencesManager.userName,
                PreferencesManager.userAvatar
            )

            comments.add(CommentItem(comment.id, comment.text, date, author))

            commentsBottomSheetTitle?.text = resources.getQuantityString(
                R.plurals.comment_plurals, comments.size, comments.size)
            commentsBottomSheetRecyclerView?.adapter?.notifyItemInserted(comments.size)
            commentsBottomSheetEditor?.text = null
        }

        fun deleteComment(commentPosition: Int) {
            comments.removeAt(commentPosition)

            commentsBottomSheetTitle?.text =
                if (comments.size == 0)
                    resources.getQuantityString(R.plurals.comment_plurals, comments.size, comments.size)
                else
                    getString(R.string.no_comments_yet)
            commentsBottomSheetRecyclerView?.adapter?.notifyItemRemoved(commentPosition)

        }

        companion object {
            private lateinit var comments: ArrayList<CommentItem>
            private lateinit var onUserClick: (userId: Int) -> Unit
            private lateinit var onImageClick: (drawable: Drawable) -> Unit
            private lateinit var addCommentCallback: (text: String) -> Unit
            private lateinit var deleteCommentCallback: (commentPosition: Int, commentId: Int) -> Unit
            private lateinit var htmlParser: HtmlParser

            fun newInstance(
                comments: ArrayList<CommentItem>,
                onUserClick: (userId: Int) -> Unit,
                onImageClick: (drawable: Drawable) -> Unit,
                addCommentCallback: (text: String) -> Unit,
                deleteCommentCallback: (commentPosition: Int, commentId: Int) -> Unit,
                htmlParser: HtmlParser
            ): CommentBottomSheetFragment {
                this.comments = comments
                this.onUserClick = onUserClick
                this.onImageClick = onImageClick
                this.addCommentCallback = addCommentCallback
                this.deleteCommentCallback = deleteCommentCallback
                this.htmlParser = htmlParser
                return CommentBottomSheetFragment()
            }
        }
    }

    class EditStatusDialogFragment : DialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? = inflater.inflate(R.layout.fragment_edit_status, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view.fragmentEditStatusOkBtn.setOnClickListener {
                editCallback(fragmentEditStatusText.text.toString())
                dialog?.dismiss()
            }
            view.fragmentEditStatusCancelBtn.setOnClickListener { dialog?.dismiss() }
        }

        companion object {
            private var status: String? = null
            private lateinit var editCallback: (status: String?) -> Unit

            fun newInstance(status: String?,
                            editCallback: (status: String?) -> Unit
            ): EditStatusDialogFragment {
                this.status = status
                this.editCallback = editCallback
                return EditStatusDialogFragment()
            }
        }
    }

    class PostMenuBottomSheetFragment : BottomSheetDialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? = inflater.inflate(R.layout.fragment_post_menu_bottom_sheet, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            fragmentPostMenuBottomSheetDelete.setOnClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(R.string.delete_this_post)
                    setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    setPositiveButton(R.string.ok) { dialog, _ ->
                        onDelete()
                        dialog.dismiss() // Dismiss AlertDialog
                        dismiss() // Dismiss BottomSheetDialog
                    }
                }.show()
            }

            fragmentPostMenuBottomSheetEdit.setOnClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    val dialogView = LayoutInflater.from(context)
                        .inflate(R.layout.fragment_post_editor, null, false)
                    setView(dialogView)
                    setTitle(R.string.edit_post)
                    dialogView.fragmentPostEditorTitle.setText(title)
                    dialogView.fragmentPostEditorText.setText(text)

                    dialogView.fragmentPostEditorText.addTextChangedListener {
                        if (it.toString().isNotBlank()) {
                            dialogView.fragmentPostEditorTextLayout.error = null
                        }
                    }

                    setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }

                    setPositiveButton(R.string.ok) { dialog, _ ->
                        val newTitle = dialogView.fragmentPostEditorTitle.text.toString()
                        val newText = dialogView.fragmentPostEditorText.text.toString()
                        if (text.isBlank()) {
                            dialogView.fragmentPostEditorTextLayout.error =
                                context.getString(R.string.this_field_is_required)
                        } else {
                            dialogView.fragmentPostEditorTextLayout.error = null
                            onEdit(newTitle, newText)
                            dialog.dismiss() // Dismiss AlertDialog
                            dismiss() // Dismiss BottomSheetDialog
                        }
                    }
                }.show()
            }
        }

        companion object {
            private var title: String? = null
            private lateinit var text: String
            private lateinit var onDelete: () -> Unit
            private lateinit var onEdit: (title: String?, text: String) -> Unit

            fun newInstance(title: String?,
                            text: String,
                            onDelete: () -> Unit,
                            onEdit: (title: String?, text: String) -> Unit
            ): PostMenuBottomSheetFragment {
                this.title = title
                this.text = text
                this.onDelete = onDelete
                this.onEdit = onEdit
                return PostMenuBottomSheetFragment()
            }
        }
    }
}
