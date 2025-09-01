package personal.cx.point.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import personal.cx.point.R
import personal.cx.point.databinding.FragmentDownloadBinding

class DownloadFragment : Fragment() {

    private var _binding: FragmentDownloadBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val downloadViewModel =
            ViewModelProvider(this).get(DownloadViewModel::class.java)

        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //测试数据
        val items = listOf(
            Item(0, R.drawable.baseline_filter_drama_24, "Test0", "TestInfo0", "127.0.0.1"),
            Item(2, R.drawable.baseline_folder_open_24, "Test1", "TestInfo1", "127.0.0.1"),
            Item(3, R.drawable.baseline_file_24, "Test2", "TestInfo2", "127.0.0.1"),
            Item(4, R.drawable.baseline_filter_drama_24, "Test0", "TestInfo0", "127.0.0.1"),
            Item(5, R.drawable.baseline_folder_open_24, "Test1", "TestInfo1", "127.0.0.1"),
            Item(6, R.drawable.baseline_file_24, "Test2", "TestInfo2", "127.0.0.1"),
            Item(7, R.drawable.baseline_filter_drama_24, "Test0", "TestInfo0", "127.0.0.1"),
            Item(8, R.drawable.baseline_folder_open_24, "Test1", "TestInfo1", "127.0.0.1"),
            Item(9, R.drawable.baseline_file_24, "Test2", "TestInfo2", "127.0.0.1"),
            Item(10, R.drawable.baseline_filter_drama_24, "Test0", "TestInfo0", "127.0.0.1"),
            Item(11, R.drawable.baseline_folder_open_24, "Test1", "TestInfo1", "127.0.0.1"),
            Item(12, R.drawable.baseline_file_24, "Test2", "TestInfo2", "127.0.0.1"),
            Item(13, R.drawable.baseline_filter_drama_24, "Test0", "TestInfo0", "127.0.0.1"),
            Item(14, R.drawable.baseline_folder_open_24, "Test1", "TestInfo1", "127.0.0.1"),
            Item(15, R.drawable.baseline_file_24, "Test2", "TestInfo2", "127.0.0.1")
        )
        //绑定适配器
        binding.downloadList.adapter = DownloadAdapter(requireContext(), items)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}