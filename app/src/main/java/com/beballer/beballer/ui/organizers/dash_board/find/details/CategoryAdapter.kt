import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.data.model.GetEventDetailsCategory
import com.beballer.beballer.databinding.ItemLayoutAddCategoryBinding
import com.beballer.beballer.databinding.ItemLayoutCategoriesBinding

class CategoryAdapter(
    private val list: MutableList<GetEventDetailsCategory>,
    private val onAddClick: () -> Unit,
    private val onCategoryClick: (GetEventDetailsCategory) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_CATEGORY = 0
        const val TYPE_ADD = 1
        const val MAX_ITEMS = 6
    }

    override fun getItemCount(): Int = MAX_ITEMS

    override fun getItemViewType(position: Int): Int {
        return if (position < list.size) TYPE_CATEGORY else TYPE_ADD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {

            TYPE_CATEGORY -> {
                val binding = ItemLayoutCategoriesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                CategoryViewHolder(binding)
            }

            else -> {
                val binding = ItemLayoutAddCategoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AddViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is CategoryViewHolder -> {
                val item = list[position]
                holder.bind(item)
            }

            is AddViewHolder -> {
                holder.bind()
            }
        }
    }

    // ---------------- ViewHolders ----------------

    inner class CategoryViewHolder(
        private val binding: ItemLayoutCategoriesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GetEventDetailsCategory) {

            binding.title.text = item.name

            // ✅ Click handled here (clean)
            binding.root.setOnClickListener {
                onCategoryClick.invoke(item)
            }
        }
    }

    inner class AddViewHolder(
        private val binding: ItemLayoutAddCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {

            binding.root.setOnClickListener {
                onAddClick.invoke()
            }
        }
    }
}