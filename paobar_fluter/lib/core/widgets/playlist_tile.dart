import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/features/playlists/domain/entities/playlist.dart';

class PlaylistTile extends StatelessWidget {
  const PlaylistTile({required this.playlist, required this.onTap, super.key});

  final Playlist playlist;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final gradient = playlist.gradient;
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                gradient: (gradient != null && gradient.length >= 2)
                    ? LinearGradient(
                        colors: gradient
                            .take(2)
                            .map(_parseHex)
                            .toList(),
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      )
                    : const LinearGradient(
                        colors: [Color(0xFF444444), Color(0xFF222222)],
                      ),
                borderRadius: BorderRadius.circular(10),
              ),
              alignment: Alignment.center,
              child: const Icon(LucideIcons.listMusic, color: Colors.white, size: 22),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    playlist.name,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    '${playlist.songCount} 首',
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ),
            const Icon(LucideIcons.chevronRight, size: 18, color: AppColors.textSecondary),
          ],
        ),
      ),
    );
  }

  Color _parseHex(String s) {
    final clean = s.replaceAll('#', '');
    final v = int.tryParse(clean.length == 6 ? 'FF$clean' : clean, radix: 16);
    return v == null ? AppColors.backgroundCard : Color(v);
  }
}
